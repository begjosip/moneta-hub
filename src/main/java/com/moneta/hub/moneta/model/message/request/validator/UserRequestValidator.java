package com.moneta.hub.moneta.model.message.request.validator;

import jakarta.validation.groups.Default;

public interface UserRequestValidator {

    interface Login extends Default {

    }

    interface Register extends Default {

    }

    interface PasswordReset extends Default {

    }

    interface NewPassword extends Default {

    }

    interface PasswordChange extends Default {

    }
}
