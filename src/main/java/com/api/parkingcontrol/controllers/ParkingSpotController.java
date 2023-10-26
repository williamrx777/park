package com.api.parkingcontrol.controllers;


import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.api.parkingcontrol.dtos.ParkingSpotDTO;
import com.api.parkingcontrol.models.ParkingSpotModel;
import com.api.parkingcontrol.service.ParkingSpotService;

import jakarta.validation.Valid;

@RestController
@CrossOrigin(origins = "*")
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
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Conflict: Parking Spot is already in use!");
        }
		
		if(parkingSpotService.existsByApartmentAndBlock(parkingSpotDTO.getApartment(), parkingSpotDTO.getBlock())){
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Conflict: Parking Spot already registered for this apartment/block!");
        }
		
		var parkingSpotModel = new ParkingSpotModel();
		BeanUtils.copyProperties(parkingSpotDTO, parkingSpotModel);
		parkingSpotModel.setRegistrationDate(LocalDateTime.now(ZoneId.of("UTC")));
		return ResponseEntity.status(HttpStatus.CREATED).body(parkingSpotService.post(parkingSpotModel));
	}
	
	@GetMapping
    public ResponseEntity<Page<ParkingSpotModel>> getAllParkingSpots(@PageableDefault(page = 0, size = 10, sort = "id", direction = Sort.Direction.ASC) Pageable pageable){
        return ResponseEntity.status(HttpStatus.OK).body(parkingSpotService.findAll(pageable));
    }
	
	@GetMapping("/{id}")
	public ResponseEntity<Object> getOne(@PathVariable(value = "id") UUID id){
		Optional<ParkingSpotModel> parkingModel = parkingSpotService.getOne(id);
				if(!parkingModel.isPresent()) {
					return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Parking Spot not found");
				}
					return ResponseEntity.status(HttpStatus.OK).body(parkingSpotService.getOne(id));						
	}
	
	@PutMapping("/{id}")
	public ResponseEntity<Object> update(@PathVariable(value = "id") UUID id, @RequestBody @Valid ParkingSpotDTO parkingSpotDTO){
		Optional<ParkingSpotModel> parkingSpot = parkingSpotService.getOne(id);
		if(!parkingSpot.isPresent()) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Parking Spot not found");
		}
		var parkingModel0 = parkingSpot.get();
		BeanUtils.copyProperties(parkingSpotDTO, parkingModel0);
		parkingModel0.setId(parkingSpot.get().getId());
		parkingModel0.setRegistrationDate(parkingSpot.get().getRegistrationDate());
		return ResponseEntity.status(HttpStatus.OK).body(parkingSpotService.post(parkingModel0));
	}
	
	@DeleteMapping("/{id}")
	public ResponseEntity<Object> delete(@PathVariable(value = "id") UUID id){
		Optional<ParkingSpotModel> parkingModel = parkingSpotService.getOne(id);
		if(parkingModel.isEmpty()) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Parking Spot not found");
		}else {
			parkingSpotService.delete(parkingModel.get());
			return ResponseEntity.status(HttpStatus.OK).body("Parking Spot deleted successfully.");
		}
		
	}
	
}
