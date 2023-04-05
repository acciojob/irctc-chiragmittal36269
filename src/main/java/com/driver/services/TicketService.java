package com.driver.services;


import com.driver.EntryDto.BookTicketEntryDto;
import com.driver.model.Passenger;
import com.driver.model.Ticket;
import com.driver.model.Train;
import com.driver.repository.PassengerRepository;
import com.driver.repository.TicketRepository;
import com.driver.repository.TrainRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class TicketService {

    @Autowired
    TicketRepository ticketRepository;

    @Autowired
    TrainRepository trainRepository;

    @Autowired
    PassengerRepository passengerRepository;


    public Integer bookTicket(BookTicketEntryDto bookTicketEntryDto)throws Exception {

        //Check for validity
        //Use bookedTickets List from the TrainRepository to get bookings done against that train
        // Incase the there are insufficient tickets
        // throw new Exception("Less tickets are available");
        //otherwise book the ticket, calculate the price and other details
        //Save the information in corresponding DB Tables
        //Fare System : Check problem statement
        //Incase the train doesn't pass through the requested stations
        //throw new Exception("Invalid stations");
        //Save the bookedTickets in the train Object
        //Also in the passenger Entity change the attribute bookedTickets by using the attribute bookingPersonId.
        //And the end return the ticketId that has come from db

        Train train = trainRepository.findById(bookTicketEntryDto.getTrainId()).get();
        int numberOfSeatsWantToBook = bookTicketEntryDto.getNoOfSeats();
        List<Ticket> ticketList = train.getBookedTickets();
        int bookedSeats = 0;

        for(Ticket ticket : ticketList) {
            bookedSeats += ticket.getPassengersList().size(); //multiple passenger in single ticket
        }

        if (bookedSeats + numberOfSeatsWantToBook > train.getNoOfSeats()) {
            throw new Exception("Less tickets are available");
        }

        String[] route = train.getRoute().split(",");
        int start = 0;
        int end = 0;
        boolean marker = true;
        boolean marker2 = true;
        for (int i = 0; i < route.length; i++) {
            if (route[i].equals(String.valueOf(bookTicketEntryDto.getFromStation()))) {
                start = i;
                marker = false;
            }
            if (route[i].equals(String.valueOf(bookTicketEntryDto.getToStation()))) {
                end = i;
                marker2 = false;
            }
        }
        if (marker || marker2) {
            throw new Exception("Invalid stations");
        }

        List<Passenger> passengers = new ArrayList<>();
        List<Integer> ids = bookTicketEntryDto.getPassengerIds();
        for (Integer i : ids) {
            passengers.add(passengerRepository.findById(i).get());
        }

        Ticket ticket = new Ticket();
        ticket.setPassengersList(passengers);
        ticket.setFromStation(bookTicketEntryDto.getFromStation());
        ticket.setToStation(bookTicketEntryDto.getToStation());
        ticket.setTotalFare(numberOfSeatsWantToBook *(end - start) * 300);
        ticket.setTrain(train);

        // object
        Passenger passenger2 = passengerRepository.findById(bookTicketEntryDto.getBookingPersonId()).get();
        passenger2.getBookedTickets().add(ticket);

        // object
        train.getBookedTickets().add(ticket);
//        train.setNoOfSeats(train.getNoOfSeats() - numberOfSeatsWantToBook);

//        trainRepository.save(train);
        Ticket updatedTicket = ticketRepository.save(ticket);

        return updatedTicket.getTicketId();

    }
}
