package com.inmobiliaria.cloud.service;

import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.firebase.FirebaseApp;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@Service
public class FirestoreService {

    private static final String COLLECTION = "registros";

    public List<Map<String, Object>> findAll(String search) throws ExecutionException, InterruptedException {
        QuerySnapshot snapshot = firestore().collection(COLLECTION).get().get();
        String normalized = search == null ? "" : search.trim().toLowerCase(Locale.ROOT);
        List<Map<String, Object>> records = new ArrayList<>();
        for (QueryDocumentSnapshot document : snapshot.getDocuments()) {
            Map<String, Object> record = new LinkedHashMap<>(document.getData());
            record.put("id", document.getId());
            if (normalized.isBlank() || record.values().stream()
                    .anyMatch(value -> String.valueOf(value).toLowerCase(Locale.ROOT).contains(normalized))) {
                records.add(record);
            }
        }
        return records;
    }

    public Map<String, Object> findById(String id) throws ExecutionException, InterruptedException {
        DocumentSnapshot document = firestore().collection(COLLECTION).document(requireId(id)).get().get();
        if (!document.exists())
            return null;
        Map<String, Object> record = new LinkedHashMap<>(document.getData());
        record.put("id", document.getId());
        return record;
    }

    public String create(Map<String, String> data) throws ExecutionException, InterruptedException {
        validate(data);
        DocumentReference reference = firestore().collection(COLLECTION).document();
        reference.create(new LinkedHashMap<>(data)).get();
        return reference.getId();
    }

    public void update(String id, Map<String, String> data) throws ExecutionException, InterruptedException {
        validate(data);
        firestore().collection(COLLECTION).document(requireId(id)).set(new LinkedHashMap<>(data)).get();
    }

    public void delete(String id) throws ExecutionException, InterruptedException {
        firestore().collection(COLLECTION).document(requireId(id)).delete().get();
    }

    private Firestore firestore() {
        if (FirebaseApp.getApps().isEmpty()) {
            throw new IllegalStateException(
                    "Firebase no esta configurado. Defina GOOGLE_APPLICATION_CREDENTIALS con la ruta del JSON de cuenta de servicio.");
        }
        return FirestoreClient.getFirestore();
    }

    private String requireId(String id) {
        if (id == null || id.isBlank() || !id.matches("[A-Za-z0-9_-]+")) {
            throw new IllegalArgumentException("Identificador de registro no valido.");
        }
        return id;
    }

    private void validate(Map<String, String> data) {
        if (data == null || data.isEmpty() || data.keySet().stream().anyMatch(key -> key == null || key.isBlank())) {
            throw new IllegalArgumentException("El registro debe contener al menos un campo con nombre.");
        }
    }
}
