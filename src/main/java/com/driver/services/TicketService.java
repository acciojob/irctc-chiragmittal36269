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


        int bookedSeats = 0;
        List<Integer> passenger1 = bookTicketEntryDto.getPassengerIds();

        List<Ticket> ticketList = train.getBookedTickets();
        for(Ticket t1 : ticketList) {
            List<Passenger> p2 = t1.getPassengersList();
            for (Integer i : passenger1) {
                Passenger p1 = passengerRepository.findById(i).get();
                if (p2.contains(p1)) {
                    bookedSeats++;
                    break;
                }
            }
        }
        int seats = train.getNoOfSeats()-bookedSeats;


        if (bookTicketEntryDto.getNoOfSeats() > seats) {
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

        Ticket ticket = new Ticket();

//        List<Passenger> passengers = new ArrayList<>();
//        for (Integer i : bookTicketEntryDto.getPassengerIds()) {
//            ticket.getPassengersList().add(passengerRepository.findById(i).get());
//        }
        Passenger passenger = passengerRepository.findById(bookTicketEntryDto.getBookingPersonId()).get();
        ticket.getPassengersList().add(passenger);
        ticket.setFromStation(bookTicketEntryDto.getFromStation());
        ticket.setToStation(bookTicketEntryDto.getToStation());
        ticket.setTotalFare((end - start) * 300);
        ticket.setTrain(train);
        Ticket updatedTicket = ticketRepository.save(ticket);

        Passenger passenger2 = new Passenger();
        passenger2.getBookedTickets().add(ticket);

        train.getBookedTickets().add(ticket);
        train.setNoOfSeats(train.getNoOfSeats()-1);

        return updatedTicket.getTicketId();

    }
}
