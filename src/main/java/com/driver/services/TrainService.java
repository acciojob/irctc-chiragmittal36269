package com.driver.services;

import com.driver.EntryDto.AddTrainEntryDto;
import com.driver.EntryDto.SeatAvailabilityEntryDto;
import com.driver.model.Passenger;
import com.driver.model.Station;
import com.driver.model.Ticket;
import com.driver.model.Train;
import com.driver.repository.TrainRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class TrainService {

    @Autowired
    TrainRepository trainRepository;

    public Integer addTrain(AddTrainEntryDto trainEntryDto) {

        //Add the train to the trainRepository
        //and route String logic to be taken from the Problem statement.
        //Save the train and return the trainId that is generated from the database.
        //Avoid using the lombok library

        Train train = new Train();

        List<Station> stationRoute = trainEntryDto.getStationRoute();
        StringBuilder ans = new StringBuilder();
        for (Station s : stationRoute) {
            ans.append(s).append(",");
        }
        ans.deleteCharAt(ans.length()-1);


        train.setRoute(ans.toString());
        train.setDepartureTime(trainEntryDto.getDepartureTime());
        train.setNoOfSeats(trainEntryDto.getNoOfSeats());

        Train updatedTrain = trainRepository.save(train);

        return updatedTrain.getTrainId();
    }

    public Integer calculateAvailableSeats(SeatAvailabilityEntryDto seatAvailabilityEntryDto){

        //Calculate the total seats available
        //Suppose the route is A B C D
        //And there are 2 seats avaialble in total in the train
        //and 2 tickets are booked from A to C and B to D.
        //The seat is available only between A to C and A to B. If a seat is empty between 2 station it will be counted to our final ans
        //even if that seat is booked post the destStation or before the boardingStation
        //Inshort : a train has totalNo of seats and there are tickets from and to different locations
        //We need to find out the available seats between the given 2 stations.

//        Train train = trainRepository.findById(seatAvailabilityEntryDto.getTrainId()).get();
//        int totalNumberOfSeatsInTrain = train.getNoOfSeats();
//
//        int bookedSeats = 0;
//        List<Ticket> ticketList = train.getBookedTickets();
//        for(Ticket ticket : ticketList) {
//            bookedSeats += ticket.getPassengersList().size();
//        }
//
//        int seatsAvailable = totalNumberOfSeatsInTrain - bookedSeats;


        int availableSeats;

        Train train = trainRepository.findById(seatAvailabilityEntryDto.getTrainId()).get();
        int totalSeats = train.getNoOfSeats();

        List<Ticket> BookedTickets = train.getBookedTickets();

        String fromStation = seatAvailabilityEntryDto.getFromStation().toString();
        String toStation = seatAvailabilityEntryDto.getToStation().toString();

        int availableBetStations = 0;

        for(Ticket ticket : BookedTickets){
            if(ticket.getToStation().toString().equals(fromStation)){
                availableBetStations += ticket.getPassengersList().size();
            }
            if(ticket.getFromStation().toString().equals(toStation)){
                availableBetStations += ticket.getPassengersList().size();
            }
        }

        int totalpassengers = 0;
        for(Ticket ticket : BookedTickets){
            totalpassengers += ticket.getPassengersList().size();
        }


        int SeatsNotAvailable = totalpassengers - availableBetStations;
        availableSeats = totalSeats-SeatsNotAvailable;

        return availableSeats;
    }

    public Integer calculatePeopleBoardingAtAStation(Integer trainId,Station station) throws Exception {

        //We need to find out the number of people who will be boarding a train from a particular station
        //if the trainId is not passing through that station
        //throw new Exception("Train is not passing from this station");
        //  in a happy case we need to find out the number of such people.

        Train train = trainRepository.findById(trainId).get();
        String route = train.getRoute();
        String[] r = route.split(",");
        String validStation = null;
        for (String s : r) {
            if (s.equals(String.valueOf(station))) {
                validStation = s;
                break;
            }
        }
        if (validStation == null) {
            throw new Exception("Train is not passing from this station");
        }

        // noOfPeoples boarding from particular station
        int noOfPeoples = 0;

        List<Ticket> ticketList = train.getBookedTickets();
        for (Ticket t : ticketList) {
            if (validStation.equals(String.valueOf(t.getFromStation()))) {
                noOfPeoples += t.getPassengersList().size();
            }
        }

        return noOfPeoples;

    }

    public Integer calculateOldestPersonTravelling(Integer trainId){

        //Throughout the journey of the train between any 2 stations
        //We need to find out the age of the oldest person that is travelling the train
        //If there are no people travelling in that train you can return 0

        int maxAgePerson = 0;
        Train train = trainRepository.findById(trainId).get();
        List<Ticket> ticketList = train.getBookedTickets();
        for(Ticket ticket : ticketList) {
            List<Passenger> passengers = ticket.getPassengersList();
            for (Passenger passenger : passengers) {
                maxAgePerson = Math.max(maxAgePerson, passenger.getAge());
            }
        }
        return maxAgePerson;
    }

    public List<Integer> trainsBetweenAGivenTime(Station station, LocalTime startTime, LocalTime endTime){

        //When you are at a particular station you need to find out the number of trains that will pass through a given station
        //between a particular time frame both start time and end time included.
        //You can assume that the date change doesn't need to be done ie the travel will certainly happen with the same date (More details
        //in problem statement)
        //You can also assume the seconds and milli seconds value will be 0 in a LocalTime format.

        List<Integer> trainsBetween = new ArrayList<>();
        List<Train> trains = trainRepository.findAll();

        for(Train train : trains) {
            int i = 0;
            String[] route = train.getRoute().split(",");
            for (String rt : route) {
                if (rt.equals(String.valueOf(station))){
                    int startTimeInMin = (startTime.getHour() * 60) + startTime.getMinute();
                    int lastTimeInMin = (endTime.getHour() * 60) + endTime.getMinute();


                    int departureTimeInMin = (train.getDepartureTime().getHour() * 60) + train.getDepartureTime().getMinute();
                    int reachingTimeInMin  = departureTimeInMin + (i * 60);
                    if(reachingTimeInMin>=startTimeInMin && reachingTimeInMin<=lastTimeInMin) {
                        trainsBetween.add(train.getTrainId());
                    }
                }
                i++;
            }
        }

        return trainsBetween;
    }

}
