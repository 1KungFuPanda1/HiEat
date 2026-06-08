package com.sky.dto.admin.employee;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
//冗余设计，后面员工注册用的EmployeeDTO
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeRegisterDTO implements Serializable {
    //序列化版本号
    //object ———— > ByteStream 序列化 （Redis缓存，网络传输）
    //ByteStream ----> object 反序列化
    private static final long serialVersionUID = 1L;

    private String username;

    private String name;

    private String password;

    private String phone;

    private String sex;

    private String idNumber;

    private String code;
}
