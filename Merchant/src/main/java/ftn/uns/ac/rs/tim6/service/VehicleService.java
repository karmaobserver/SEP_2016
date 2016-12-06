package ftn.uns.ac.rs.tim6.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ftn.uns.ac.rs.tim6.model.Vehicle;
import ftn.uns.ac.rs.tim6.repository.VehicleRepository;

@Service
@Transactional
public class VehicleService implements GenericService<Vehicle>{
	
	@Autowired
	private VehicleRepository vehicleRepository;

	@Override
	public List<Vehicle> getAll() {
		// TODO Auto-generated method stub
		return vehicleRepository.findAll();
	}

	@Override
	public Vehicle save(Vehicle t) {
		// TODO Auto-generated method stub
		return vehicleRepository.save(t);
	}

}