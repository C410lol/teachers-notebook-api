package com.api.notebook.models;

import com.api.notebook.enums.AuthTryEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Data
@NoArgsConstructor
public class AuthTryModel {

    private AuthTryEnum status;
    private AuthReturnModel authReturnModel;

}
