package com.example.ltm.controller;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.example.ltm.entity.Diem;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Controller
@CrossOrigin
public class DiemController {
    // List to store Diem objects
    List<Diem> danhSachDiem = new ArrayList<>();

    // Display the form for rating
    @GetMapping("/score/{name}")
    public String hienThiFormDanhGia(@PathVariable(value = "name") String name, Model model) {
        model.addAttribute("message", "Điểm đánh giá nhóm " + name);
        model.addAttribute("name", name);

        return "danhgia";
    }

    // Handle the form submission and update danhSachDiem
    @PostMapping("/result")
    public String getTongket(Model model, @RequestParam(name = "userInput") String userInput,
            @RequestParam(name = "userId") String userId) {

        try {
            int userIdValue = Integer.parseInt(userId);
            int userInputValue = Integer.parseInt(userInput);

            // Check if userId already exists in danhSachDiem
            boolean userIdExists = false;
            for (Diem diem : danhSachDiem) {
                if (diem.getId() == userIdValue) {
                    // Update the existing entry
                    diem.setDiem(userInputValue);
                    userIdExists = true;
                    break;
                }
            }

            // If userId doesn't exist, add a new entry
            if (!userIdExists) {
                Diem d = new Diem(userIdValue, userInputValue);
                danhSachDiem.add(d);
            }

            model.addAttribute("message", "Điểm các nhóm là");
            model.addAttribute("diemList", danhSachDiem);

        } catch (NumberFormatException e) {
            // Handle the case where userId or userInput is not a valid integer
            e.printStackTrace();
            model.addAttribute("error", "Nhập sai định dạng số");
        }

        return "tongket";
    }

    // API endpoint to get the ID and Diem as JSON
    @GetMapping("/api/diem")
    @ResponseBody
    public List<Map<String, Integer>> getDiemApi() {
        List<Map<String, Integer>> diemJsonList = new ArrayList<>();

        for (Diem diem : danhSachDiem) {
            Map<String, Integer> diemJson = new HashMap<>();
            diemJson.put("id", diem.getId());
            diemJson.put("diem", diem.getDiem());
            diemJsonList.add(diemJson);
        }

        return diemJsonList;
    }

    @Autowired
    private JavaMailSender mailSender;

    @PostMapping("/send-email")
    public ResponseEntity<String> sendEmail(@RequestBody Map<String, Object> payload) {
        String email = (String) payload.get("email");
        String[] teamNames = ((List<String>) payload.get("teamNames")).toArray(new String[0]);
        String[] scores = ((List<String>) payload.get("scores")).toArray(new String[0]);

        try {
            // Create workbook and sheet
            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("BangDiemNhom");

            // Create header
            Row headerRow = sheet.createRow(0);
            Cell cellTeamName = headerRow.createCell(0);
            cellTeamName.setCellValue("Tên Nhóm");

            Cell cellScore = headerRow.createCell(1);
            cellScore.setCellValue("Điểm");

            // Populate data dynamically
            for (int i = 0; i < teamNames.length; i++) {
                Row dataRow = sheet.createRow(i + 1);
                dataRow.createCell(0).setCellValue(teamNames[i]);
                dataRow.createCell(1).setCellValue(Double.parseDouble(scores[i])); // Assuming scores are numeric
            }

            // Write workbook to ByteArrayOutputStream
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            workbook.write(bos);
            bos.close();

            // Attach Excel file to email
            byte[] excelBytes = bos.toByteArray();

            // Create MimeMessage
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setTo(email);
            helper.setSubject("Bảng Điểm Nhóm ABC");
            helper.setText("Nội dung email: Bảng điểm của nhóm ABsC...");

            // Attach Excel file to email
            helper.addAttachment("BangDiemNhom.xlsx", new ByteArrayResource(excelBytes));

            // Send email
            mailSender.send(message); 

            // Return successful response
            return new ResponseEntity<>("Email đã được gửi!", HttpStatus.OK);
        } catch (IOException | MessagingException e) {
            // Handle specific exceptions
            e.printStackTrace();
            // Return error response
            return new ResponseEntity<>("Có lỗi xảy ra khi gửi email.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
