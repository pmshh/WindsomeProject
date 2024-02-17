package com.windsome.exception;

public class ProductImageDeletionException extends RuntimeException{
    ProductImageDeletionException() {
    }

    public ProductImageDeletionException(String message) {
        super(message);
    }
}
