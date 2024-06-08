package com.example.courtstar.controller;

import com.example.courtstar.dto.request.ApiResponse;
import com.example.courtstar.dto.request.CentreManagerRequest;
import com.example.courtstar.dto.request.CentreRequest;
import com.example.courtstar.dto.response.CentreManagerResponse;
import com.example.courtstar.dto.response.CentreResponse;
import com.example.courtstar.services.CentreManagerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/manager")
public class CentreManagerController {
    @Autowired
    private CentreManagerService centreManagerService;


    @PutMapping("/updateInfo/{account_id}")
    public ApiResponse<CentreManagerResponse> updateManagerCentre(@PathVariable int account_id ,@RequestBody CentreManagerRequest request) {
        CentreManagerResponse centreManager = centreManagerService.updateInformation(account_id,request);
        return ApiResponse.<CentreManagerResponse>builder()
                .data(centreManager)
                .build();
    }
    @PostMapping("/create")
    public ApiResponse<CentreResponse> createCentre(@RequestBody CentreRequest request){
        CentreResponse centreResponse = centreManagerService.addCentre(request);
        return ApiResponse.<CentreResponse>builder()
                .data(centreResponse)
                .build();
    }
    
}