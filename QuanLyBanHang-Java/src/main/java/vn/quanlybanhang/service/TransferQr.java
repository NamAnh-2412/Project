package vn.quanlybanhang.service;

import java.math.BigDecimal;

public record TransferQr(
        String bankName,
        String accountName,
        String accountNumber,
        String content,
        BigDecimal amount,
        String imageUrl,
        boolean ready,
        String hint) {
}
