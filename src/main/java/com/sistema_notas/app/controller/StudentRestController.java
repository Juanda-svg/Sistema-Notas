package com.sistema_notas.app.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.sistema_notas.app.repositorio.UserRepository;
import com.sistema_notas.app.repositorio.CourseRepository;
import com.sistema_notas.app.repositorio.EnrollmentRepository;
import com.sistema_notas.app.repositorio.GradeRepository;
import com.sistema_notas.app.entidad.User;
import com.sistema_notas.app.entidad.Course;
import com.sistema_notas.app.entidad.Enrollment;
import com.sistema_notas.app.entidad.Grade;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.properties.TextAlignment;
import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;



@RestController
@RequestMapping("/api/student")
public class StudentRestController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Autowired
    private GradeRepository gradeRepository;

    @GetMapping
    public String studentPanel(Model model, Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) {
            return "redirect:/login";
        }

        String userId = authentication.getName();
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        model.addAttribute("fullName", user.getFullName());
        model.addAttribute("role", user.getRole());
        model.addAttribute("userId", user.getUserId());
        model.addAttribute("phone", user.getPhone() != null ? user.getPhone() : "");

        var enrollments = enrollmentRepository.findByStudentId(userId);
        List<Course> enrolledCourses = courseRepository.findAllById(enrollments.stream()
                .map(enrollment -> enrollment.getCourseId())
                .collect(Collectors.toList()));
        model.addAttribute("courses", enrolledCourses);

       
        List<Map<String, Object>> gradesSummary = enrollments.stream()
                .map(e -> {
                    Course course = courseRepository.findById(e.getCourseId()).orElse(null);
                    Optional<Grade> gradeOptional = gradeRepository.findByStudentIdAndCourseId(userId, e.getCourseId());
                    Grade grade = gradeOptional.orElse(null);
                    Map<String, Object> gradeData = new HashMap<>();
                    gradeData.put("courseName", course != null ? course.getName() : "Desconocido");
                    gradeData.put("grade", grade != null ? grade.getScore() : "N/A");
                    gradeData.put("status", grade != null && grade.getScore() < 3.0 ? "Reprobado" : "Aprobado");


                    return gradeData;
                })
                .collect(Collectors.toList());

        model.addAttribute("gradesSummary", gradesSummary);

        
        boolean hasFailedCourses = gradesSummary.stream()
                .anyMatch(grade -> grade.get("grade") instanceof Double && (Double) grade.get("grade") < 3.0);
        model.addAttribute("hasFailedCourses", hasFailedCourses);

        model.addAttribute("availableCourses", courseRepository.findAll());

        return "student";
    }

    @PostMapping("/enroll")
    public ResponseEntity<String> enrollInCourse(@RequestParam String courseId, Authentication authentication) {
        User user = userRepository.findByUserId(authentication.getName()).orElse(null);
        if (user == null) {
            return ResponseEntity.badRequest().body("Error: Usuario no encontrado");
        }

        Enrollment enrollment = new Enrollment();
        enrollment.setStudentId(user.getUserId());
        enrollment.setCourseId(courseId);
        enrollmentRepository.save(enrollment);
        return ResponseEntity.ok("Inscripción exitosa");
    }

    @PostMapping("/update-phone")
    public ResponseEntity<String> updatePhone(@RequestBody Map<String, String> payload, Authentication authentication) {
        try {
          
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                     .body("Error: Usuario no autenticado");
            }

           
            String userId = authentication.getName();
            Optional<User> optionalUser = userRepository.findByUserId(userId);

            if (optionalUser.isEmpty()) {
                return ResponseEntity.badRequest().body("Error: Usuario no encontrado");
            }

            User user = optionalUser.get();

           
            String phone = payload.get("phone");
            if (phone == null || phone.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Error: El número telefónico no puede estar vacío");
            }

           
            user.setPhone(phone.trim());
            userRepository.save(user);

            return ResponseEntity.ok("Número telefónico actualizado exitosamente");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body("Error al actualizar el número telefónico: " + e.getMessage());
        }
    }

    @GetMapping("/certificate")
    public ResponseEntity<byte[]> generateCertificate(Authentication authentication) throws Exception {
        User user = userRepository.findByUserId(authentication.getName()).orElse(null);
        if (user == null) {
            return ResponseEntity.badRequest().build();
        }

        List<Enrollment> enrollments = enrollmentRepository.findByStudentId(user.getUserId());
        List<Map<String, String>> courses = enrollments.stream()
                .map(e -> {
                    Course course = courseRepository.findById(e.getCourseId()).orElse(null);
                    Map<String, String> courseData = new HashMap<>();
                    courseData.put("name", course != null ? course.getName() : "Desconocido");
                    courseData.put("schedule", course != null ? course.getSchedule() : "N/A");
                    return courseData;
                })
                .collect(Collectors.toList());

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(baos);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);

        Paragraph title = new Paragraph("Certificado de Matrícula")
                .setTextAlignment(TextAlignment.CENTER)
                .setBold()
                .setFontSize(20);
        document.add(title);

        document.add(new Paragraph("\n"));

        document.add(new Paragraph("Se certifica que el estudiante:"));
        document.add(new Paragraph("Nombre: " + user.getFullName()).setBold());
        document.add(new Paragraph("ID: " + user.getUserId()).setBold());
        document.add(new Paragraph("está matriculado en las siguientes materias para el período académico actual:"));
        document.add(new Paragraph("\n"));

        Table table = new Table(2);
        table.addHeaderCell(new Cell().add(new Paragraph("Materia").setBold()));
        table.addHeaderCell(new Cell().add(new Paragraph("Horario").setBold()));
        for (Map<String, String> course : courses) {
            table.addCell(new Cell().add(new Paragraph(course.get("name"))));
            table.addCell(new Cell().add(new Paragraph(course.get("schedule"))));
        }
        document.add(table);

        document.add(new Paragraph("\n"));

        String issueDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd 'de' MMMM 'de' yyyy"));
        document.add(new Paragraph("Emitido el: " + issueDate));

        document.add(new Paragraph("\n"));
        Paragraph signature = new Paragraph("Firma Autorizada")
                .setTextAlignment(TextAlignment.CENTER)
                .setBold();
        document.add(signature);

        document.close();

        byte[] pdfContent = baos.toByteArray();
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header("Content-Disposition", "attachment; filename=\"certificado_matricula_" + user.getUserId() + ".pdf\"")
                .body(pdfContent);
    }
}