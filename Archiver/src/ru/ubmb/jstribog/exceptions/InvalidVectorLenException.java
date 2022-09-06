/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ru.ubmb.jstribog.exceptions;

/**
 *
 * @author sarin
 */
public class InvalidVectorLenException extends Error{

    public InvalidVectorLenException() {
    }

    public InvalidVectorLenException(String message) {
        super(message);
    }

    public InvalidVectorLenException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidVectorLenException(Throwable cause) {
        super(cause);
    }

    public InvalidVectorLenException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
    
    
    
}
