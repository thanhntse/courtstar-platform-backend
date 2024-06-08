package com.example.courtstar.services;

import com.example.courtstar.dto.request.CentreManagerRequest;
import com.example.courtstar.dto.request.CentreRequest;
import com.example.courtstar.dto.request.CourtRequest;
import com.example.courtstar.dto.response.AccountResponse;
import com.example.courtstar.dto.response.CentreManagerResponse;
import com.example.courtstar.dto.response.CentreResponse;
import com.example.courtstar.entity.*;
import com.example.courtstar.exception.AppException;
import com.example.courtstar.exception.ErrorCode;
import com.example.courtstar.mapper.AccountMapper;
import com.example.courtstar.mapper.CentreManagerMapper;
import com.example.courtstar.mapper.CentreMapper;
import com.example.courtstar.mapper.CourtMapper;
import com.example.courtstar.repositories.*;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
@Slf4j
@EnableWebSecurity
@EnableMethodSecurity
public class CentreManagerService {
    @Autowired
    CentreManagerRepository centreManagerRepository;
    @Autowired
    CentreManagerMapper centreManagerMapper;
    @Autowired
    AccountReponsitory accountReponsitory;
    @Autowired
    CentreRepository centreRepository;
    @Autowired
    CentreMapper centreMapper;
    @Autowired
    CourtRepository courtRepository;
    @Autowired
    CourtMapper courtMapper;
    @Autowired
    ImgRepository imgRepository;
    @Autowired
    private AccountMapper accountMapper;
    @Autowired
    private SlotRepository slotRepository;

    public CentreManager addInformation(CentreManagerRequest request) {
        CentreManager centreManager = centreManagerMapper.toCentreManager(request);
        centreManager.setCentres(new ArrayList<>());
        return centreManagerRepository.save(centreManager);
    }

    public CentreManagerResponse updateInformation(int account_id, CentreManagerRequest request){
        CentreManager manager = centreManagerRepository.findByAccountId(account_id).orElseThrow(null);
        Role role= manager.getAccount().getRoles().stream().filter(i->i.getName().equals("MANAGER")).findFirst().orElse(null);
        if(role==null){
            throw new AppException(ErrorCode.ERROR_ROLE);
        }
        centreManagerMapper.updateCentre(manager,request);
        centreManagerRepository.save(manager);
        AccountResponse accountResponse = accountMapper.toAccountResponse(manager.getAccount());

        return CentreManagerResponse.builder()
                .account(accountResponse)
                .address(manager.getAddress())
                .currentBalance(manager.getCurrentBalance())
                .build();
    }

    public CentreResponse addCentre(CentreRequest request){
        var context = SecurityContextHolder.getContext();
        String name = context.getAuthentication().getName();
        Account account = accountReponsitory.findByEmail(name).orElseThrow(()->new AppException(ErrorCode.NOT_FOUND_USER));
        CentreManager manager = centreManagerRepository.findByAccountId(account.getId())
                .orElseThrow( () -> new AppException(ErrorCode.NOT_FOUND_USER));
        Role role= manager.getAccount().getRoles().stream()
                .filter( i -> i.getName().equals("MANAGER"))
                .findFirst()
                .orElse(null);
        if(role==null){
            throw new RuntimeException();
        }
        Centre centre = centreMapper.toCentre(request);

        List<Slot> slotList = generateSlots(centre);
        centre.setSlots(slotList);

        List<Image> imgList = generateImages(request, centre);

        centre.setImages(imgList);
        centre.setManager(manager);
        centreRepository.save(centre);

        List<Centre> centres = manager.getCentres();
        if (centres == null) {
            centres = new ArrayList<>();
        }
        centres.add(centre);
        manager.setCentres(centres);

        CourtRequest courtRequest = new CourtRequest();
        centre.setCourts(addCourt(centre.getId(),courtRequest));

        slotRepository.saveAll(slotList);
        imgRepository.saveAll(imgList);
        centreRepository.save(centre);
        centreManagerRepository.save(manager);

        CentreResponse centreResponse = centreMapper.toCentreResponse(centre);
        centreResponse.setManagerId(manager.getId());
        return centreResponse;
    }

    private List<Court> addCourt(int idCentre, CourtRequest request){
        Centre centre = centreRepository.findById(idCentre).orElseThrow(()->new AppException(ErrorCode.NOT_FOUND_CENTRE));

        List<Court> courts = new ArrayList<>();

        for(int i=0;i<centre.getNumberOfCourt();i++){
            Court court = courtMapper.toCourt(request.builder()
                    .courtNo(i+1)
                    .status(true)
                    .build());
            court.setCentre(centre);
            courts.add(courtRepository.save(court));
        }
        centre.setCourts(courts);
        return centreRepository.save(centre).getCourts();
    }

    private List<Image> generateImages(CentreRequest request, Centre centre) {
        AtomicInteger imageNo = new AtomicInteger(1);
        List<Image> imgList = request.getImages().stream().map(url -> {
            Image image = new Image();
            image.setUrl(url);
            image.setCentre(centre);
            image.setImageNo(imageNo.getAndIncrement());
            return image;
        }).collect(Collectors.toCollection(() -> new ArrayList<>()));

        return imgList;
    }

    private List<Slot> generateSlots(Centre centre) {
        List<Slot> slots = new ArrayList<>();
        int slotNo = 1;
        LocalTime currentTime = centre.getOpenTime();
        LocalTime closeTime = centre.getCloseTime();
        int slotDuration = centre.getSlotDuration();

        while (currentTime.plusHours(slotDuration).isBefore(closeTime) || currentTime.plusHours(slotDuration).equals(closeTime)) {
            Slot slot = Slot.builder()
                    .slotNo(slotNo)
                    .startTime(currentTime)
                    .endTime(currentTime.plusHours(slotDuration))
                    .centre(centre)
                    .build();
            slots.add(slot);

            currentTime = currentTime.plusHours(slotDuration);
            slotNo++;
        }

        return slots;
    }

}
