package com.inmobiliaria.cloud.controller;

import com.inmobiliaria.cloud.service.CsvService;
import com.inmobiliaria.cloud.service.FirestoreService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/")
public class RecordController {

    private final FirestoreService firestoreService;
    private final CsvService csvService;

    public RecordController(FirestoreService firestoreService, CsvService csvService) {
        this.firestoreService = firestoreService;
        this.csvService = csvService;
    }

    @GetMapping
    public String index(@RequestParam(defaultValue = "") String search,
            @RequestParam(required = false) String edit, Model model) {
        model.addAttribute("search", search);
        try {
            List<Map<String, Object>> records = firestoreService.findAll(search);
            model.addAttribute("records", records);
            model.addAttribute("configured", true);
            model.addAttribute("columns", records.stream().flatMap(record -> record.keySet().stream())
                    .filter(key -> !key.equals("id")).distinct().toList());
            if (edit != null && !edit.isBlank())
                model.addAttribute("editRecord", firestoreService.findById(edit));
        } catch (Exception exception) {
            model.addAttribute("records", List.of());
            model.addAttribute("columns", List.of());
            model.addAttribute("configured", false);
            model.addAttribute("configurationMessage", exception.getMessage());
        }
        return "records";
    }

    @PostMapping("/upload")
    public String upload(@RequestParam("file") MultipartFile file, RedirectAttributes redirectAttributes) {
        try {
            List<Map<String, String>> rows = csvService.read(file);
            for (Map<String, String> row : rows)
                firestoreService.create(row);
            redirectAttributes.addFlashAttribute("success", rows.size() + " registro(s) importado(s) correctamente.");
        } catch (Exception exception) {
            redirectAttributes.addFlashAttribute("error", message(exception));
        }
        return "redirect:/";
    }

    @PostMapping("/records")
    public String save(@RequestParam(required = false) String id,
            @RequestParam Map<String, String> fields, RedirectAttributes redirectAttributes) {
        fields.remove("id");
        try {
            if (id == null || id.isBlank()) {
                firestoreService.create(new LinkedHashMap<>(fields));
                redirectAttributes.addFlashAttribute("success", "Registro creado correctamente.");
            } else {
                firestoreService.update(id, new LinkedHashMap<>(fields));
                redirectAttributes.addFlashAttribute("success", "Registro actualizado correctamente.");
            }
        } catch (Exception exception) {
            redirectAttributes.addFlashAttribute("error", message(exception));
        }
        return "redirect:/";
    }

    @PostMapping("/records/{id}/delete")
    public String delete(@PathVariable String id, RedirectAttributes redirectAttributes) {
        try {
            firestoreService.delete(id);
            redirectAttributes.addFlashAttribute("success", "Registro eliminado correctamente.");
        } catch (Exception exception) {
            redirectAttributes.addFlashAttribute("error", message(exception));
        }
        return "redirect:/";
    }

    private String message(Exception exception) {
        Throwable cause = exception;
        while (cause.getCause() != null && (cause.getMessage() == null || cause.getMessage().isBlank()))
            cause = cause.getCause();
        return cause.getMessage() == null ? "No fue posible completar la operacion." : cause.getMessage();
    }
}
