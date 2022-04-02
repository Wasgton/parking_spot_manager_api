package com.example.parkingcontrol.controllers;

import com.example.parkingcontrol.DTO.ParkingSpotDTO;
import com.example.parkingcontrol.models.ParkingSpot;
import com.example.parkingcontrol.services.ParkingSpotService;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@CrossOrigin(origins = "*",maxAge = 3600)
@RequestMapping("/parking-spot")
public class ParkingSpotController {

    final ParkingSpotService parkingSpotService;

    public ParkingSpotController(ParkingSpotService parkingSpotService) {
        this.parkingSpotService = parkingSpotService;
    }

    @PostMapping
    public ResponseEntity<Object> saveParkingSpot(@RequestBody @Valid ParkingSpotDTO parkingSpotDTO){

        if(parkingSpotService.existsByLicensePlateCar(parkingSpotDTO.getLicensePlateCar())){
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Conflict: License Plate Car is already in use!");
        }
        if(parkingSpotService.existsByParkingSpotNumber(parkingSpotDTO.getParkingSpotNumber())){
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Conflict: Spot Number is already in use!");
        }
        if(parkingSpotService.existsByApartmentAndBlock(parkingSpotDTO.getApartment(),parkingSpotDTO.getBlock())){
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Conflict: Apartment is already in using a spot!");
        }

        var parkingSpotModel = new ParkingSpot();

        BeanUtils.copyProperties(parkingSpotDTO, parkingSpotModel);

        parkingSpotModel.setRegistrationDate(LocalDateTime.now(ZoneId.of("UTC")));

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(parkingSpotService.save(parkingSpotModel));

    }

    @GetMapping
    public ResponseEntity<Page<ParkingSpot>> getAllParkingSpots(@PageableDefault(page = 0, size = 10,value = 10,sort = "id",direction = Sort.Direction.ASC) Pageable pageable){
        return ResponseEntity.status(HttpStatus.OK).body(parkingSpotService.findAll(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> findById(@PathVariable(value="id") UUID id){
        Optional<ParkingSpot> parkingSpotOptional = parkingSpotService.findById(id);
        if (!parkingSpotOptional.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Parking Spot was not found");
        }

        return ResponseEntity.status(HttpStatus.OK).body(parkingSpotOptional.get());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Object> deleteParkingSpot(@PathVariable(value="id") UUID id){
        Optional<ParkingSpot> parkingSpotOptional = parkingSpotService.findById(id);
        if(!parkingSpotOptional.isPresent()){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Parking Spot was not found");
        }

        parkingSpotService.delete(parkingSpotOptional.get());
        return ResponseEntity.status(HttpStatus.OK).body("Deleted successfully");

    }

    @PutMapping("/{id}")
    public ResponseEntity<Object> updateParkingSpot(@PathVariable(value="id")UUID id, @RequestBody @Valid ParkingSpotDTO parkingSpotDTO){

        if(parkingSpotService.existsByLicensePlateCar(parkingSpotDTO.getLicensePlateCar())){
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Conflict: License Plate Car is already in use!");
        }
        if(parkingSpotService.existsByParkingSpotNumber(parkingSpotDTO.getParkingSpotNumber())){
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Conflict: Spot Number is already in use!");
        }
        if(parkingSpotService.existsByApartmentAndBlock(parkingSpotDTO.getApartment(),parkingSpotDTO.getBlock())){
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Conflict: Apartment is already in using a spot!");
        }

        Optional<ParkingSpot> parkingSpotOptional =  parkingSpotService.findById(id);

        if(!parkingSpotOptional.isPresent()){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Parking Spot not found");
        }

        var parkingSpot = new ParkingSpot();

        BeanUtils.copyProperties(parkingSpotDTO,parkingSpot);

        parkingSpot.setId(parkingSpotOptional.get().getId());
        parkingSpot.setRegistrationDate(parkingSpotOptional.get().getRegistrationDate());

        return ResponseEntity.status(HttpStatus.OK).body(parkingSpotService.save(parkingSpotOptional.get()));
    }

}
