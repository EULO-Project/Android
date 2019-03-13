package com.ulo.wallet.ui.transaction.custom;

/**
 * Created by furszy on 8/9/17.
 */

class InvalidFeeException extends Exception {

    public InvalidFeeException(String message) {
        super(message);
    }
}
