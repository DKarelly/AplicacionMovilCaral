package com.example.py_damb2026ii.util;

import android.content.Context;
import android.widget.Toast;

import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.codescan.GmsBarcodeScanner;
import com.google.mlkit.vision.codescan.GmsBarcodeScannerOptions;
import com.google.mlkit.vision.codescan.GmsBarcodeScanning;

public class QrHelper {

    public interface OnQrScannedListener {
        void onScanned(String rawValue);
        void onError(Exception e);
        void onCancelled();
    }

    public static void escanearQr(Context context, OnQrScannedListener listener) {
        if (context == null) return;

        GmsBarcodeScannerOptions options = new GmsBarcodeScannerOptions.Builder()
                .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
                .build();

        GmsBarcodeScanner scanner = GmsBarcodeScanning.getClient(context, options);

        scanner.startScan()
                .addOnSuccessListener(barcode -> {
                    String rawValue = barcode.getRawValue();
                    if (rawValue != null && !rawValue.isEmpty()) {
                        if (listener != null) {
                            listener.onScanned(rawValue);
                        }
                    } else {
                        Toast.makeText(context, "No se pudo leer el código QR", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnCanceledListener(() -> {
                    Toast.makeText(context, "Escaneo cancelado", Toast.LENGTH_SHORT).show();
                    if (listener != null) {
                        listener.onCancelled();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "Error al escanear QR: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    if (listener != null) {
                        listener.onError(e);
                    }
                });
    }
}
