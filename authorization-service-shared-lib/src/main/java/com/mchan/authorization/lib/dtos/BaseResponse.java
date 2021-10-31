package com.mchan.authorization.lib.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Base response that includes a message in case somethings goes wrong.
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class BaseResponse {

    private String message;

}
