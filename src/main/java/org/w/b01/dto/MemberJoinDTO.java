package org.w.b01.dto;

import lombok.Data;

@Data
public class MemberJoinDTO {
    private String mid;
    private String mpw;
    private String email;
    private String del;
    private boolean social;
}
