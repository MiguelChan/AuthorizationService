// Command load measures a bounded, open-loop local authenticated OAuth workload.
package main

import (
	"bytes"
	"crypto/tls"
	"crypto/x509"
	"encoding/base64"
	"encoding/json"
	"flag"
	"fmt"
	"io"
	"net/http"
	"net/url"
	"os"
	"sort"
	"sync"
	"sync/atomic"
	"time"
)

type fixture struct {
	ClientID     string `json:"clientId"`
	ClientSecret string `json:"clientSecret"`
	Token        string `json:"token"`
	Audience     string `json:"audience"`
}
type observation struct {
	Latency float64
	Status  int
	Valid   bool
	Error   string
}

func main() {
	target := flag.String("url", "https://localhost:18443/oauth/introspect", "Owned loopback token validation URL")
	fixturePath := flag.String("fixture", "", "Private disposable fixture file")
	caPath := flag.String("ca", "", "Trusted local TLS certificate")
	rate := flag.Int("rate", 1000, "Offered requests per second; 1..5000")
	seconds := flag.Int("seconds", 60, "Measurement duration; 1..300")
	concurrent := flag.Int("concurrency", 1000, "Maximum requests in flight; 1..1000")
	burst := flag.Bool("burst", false, "Release exactly concurrency requests simultaneously")
	flag.Parse()
	parsed, err := url.Parse(*target)
	if err != nil || parsed.Scheme != "https" || parsed.User != nil || parsed.RawQuery != "" || parsed.Path != "/oauth/introspect" || (parsed.Hostname() != "localhost" && parsed.Hostname() != "127.0.0.1" && parsed.Hostname() != "::1") {
		panic("Target must be the owned HTTPS loopback service")
	}
	if *rate < 1 || *rate > 5000 || *seconds < 1 || *seconds > 300 || *concurrent < 1 || *concurrent > 1000 {
		panic("Load parameters outside bounded range")
	}
	data, err := os.ReadFile(*fixturePath)
	if err != nil {
		panic(err)
	}
	var f fixture
	if json.Unmarshal(data, &f) != nil || f.Token == "" || f.ClientSecret == "" {
		panic("Invalid private fixture")
	}
	cert, err := os.ReadFile(*caPath)
	if err != nil {
		panic(err)
	}
	pool := x509.NewCertPool()
	if !pool.AppendCertsFromPEM(cert) {
		panic("Invalid local certificate")
	}
	transport := &http.Transport{MaxIdleConns: *concurrent, MaxIdleConnsPerHost: *concurrent, MaxConnsPerHost: *concurrent, TLSClientConfig: &tls.Config{MinVersion: tls.VersionTLS12, RootCAs: pool}, IdleConnTimeout: 10 * time.Second}
	defer transport.CloseIdleConnections()
	client := &http.Client{Transport: transport, Timeout: 5 * time.Second, CheckRedirect: func(_ *http.Request, _ []*http.Request) error { return http.ErrUseLastResponse }}
	basic := "Basic " + base64.StdEncoding.EncodeToString([]byte(url.QueryEscape(f.ClientID)+":"+url.QueryEscape(f.ClientSecret)))
	body := []byte(url.Values{"token": {f.Token}}.Encode())
	perform := func() observation {
		start := time.Now()
		req, _ := http.NewRequest(http.MethodPost, *target, bytes.NewReader(body))
		req.Header.Set("Authorization", basic)
		req.Header.Set("Content-Type", "application/x-www-form-urlencoded")
		response, err := client.Do(req)
		o := observation{Latency: float64(time.Since(start).Microseconds()) / 1000}
		if err != nil {
			o.Error = err.Error()
			return o
		}
		defer response.Body.Close()
		payload, err := io.ReadAll(io.LimitReader(response.Body, 65536))
		o.Status = response.StatusCode
		var claims map[string]any
		if err == nil && json.Unmarshal(payload, &claims) == nil {
			o.Valid = o.Status == 200 && claims["active"] == true && claims["aud"] == f.ClientID
		}
		if err != nil {
			o.Error = "response_body_read_failed"
		}
		o.Latency = float64(time.Since(start).Microseconds()) / 1000
		return o
	}
	// Warm up the measured authorization path; do not include it in throughput.
	for i := 0; i < 100; i++ {
		if !perform().Valid {
			panic("Warmup authorization failed")
		}
	}
	if *burst {
		var warm sync.WaitGroup
		failures := atomic.Int64{}
		for i := 0; i < *concurrent; i++ {
			warm.Add(1)
			go func() {
				defer warm.Done()
				if !perform().Valid {
					failures.Add(1)
				}
			}()
		}
		warm.Wait()
		if failures.Load() != 0 {
			panic("Concurrent warmup failed")
		}
		time.Sleep(time.Second)
	}
	jobs := make(chan time.Time, *concurrent)
	observations := make(chan observation, *rate**seconds+*concurrent)
	var workers sync.WaitGroup
	var ready sync.WaitGroup
	gate := make(chan struct{})
	if *burst {
		ready.Add(*concurrent)
	} else {
		close(gate)
	}
	var inflight, maxflight atomic.Int64
	for i := 0; i < *concurrent; i++ {
		workers.Add(1)
		go func() {
			defer workers.Done()
			for scheduled := range jobs {
				if *burst {
					ready.Done()
					<-gate
				}
				n := inflight.Add(1)
				for {
					old := maxflight.Load()
					if n <= old || maxflight.CompareAndSwap(old, n) {
						break
					}
				}
				o := perform()
				o.Latency = float64(time.Since(scheduled).Microseconds()) / 1000
				inflight.Add(-1)
				observations <- o
			}
		}()
	}
	n := *rate * *seconds
	if *burst {
		n = *concurrent
	}
	start := time.Now()
	dropped := 0
	for i := 0; i < n; i++ {
		due := start
		if !*burst {
			due = start.Add(time.Duration(i) * time.Second / time.Duration(*rate))
			if wait := time.Until(due); wait > 0 {
				time.Sleep(wait)
			}
		}
		select {
		case jobs <- due:
		default:
			dropped++
		}
	}
	close(jobs)
	if *burst {
		ready.Wait()
		close(gate)
	}
	workers.Wait()
	close(observations)
	elapsed := time.Since(start).Seconds()
	latencies := []float64{}
	codes := map[int]int{}
	errors := map[string]int{}
	valid, unexpected := 0, 0
	for o := range observations {
		latencies = append(latencies, o.Latency)
		codes[o.Status]++
		if o.Error != "" {
			errors[o.Error]++
		}
		if o.Valid {
			valid++
		} else {
			unexpected++
		}
	}
	sort.Float64s(latencies)
	percentile := func(p float64) float64 {
		if len(latencies) == 0 {
			return 0
		}
		i := int(float64(len(latencies)-1) * p)
		return latencies[i]
	}
	mode := "open-loop"
	if *burst {
		mode = "simultaneous-burst"
	}
	result := map[string]any{"mode": mode, "offered_rps": *rate, "scheduled": n, "completed": len(latencies), "valid_authorized": valid, "unexpected_errors": unexpected, "dropped": dropped, "elapsed_seconds": elapsed, "completed_rps": float64(valid) / elapsed, "statuses": codes, "transport_errors": errors, "max_inflight": maxflight.Load(), "p50_ms": percentile(.5), "p95_ms": percentile(.95), "p99_ms": percentile(.99), "error_fraction": float64(unexpected+dropped) / float64(n), "tls_verified": true}
	encoded, _ := json.MarshalIndent(result, "", "  ")
	fmt.Println(string(encoded))
	if float64(unexpected+dropped)/float64(n) >= .01 || percentile(.95) > 250 || (!*burst && float64(valid)/elapsed < float64(*rate)*.99) {
		os.Exit(1)
	}
}
