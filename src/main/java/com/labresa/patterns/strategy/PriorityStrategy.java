package com.labresa.patterns.strategy;

//import java.util.Comparator;

import com.labresa.model.Reservation;

import java.util.List;

public interface PriorityStrategy {

    Reservation resolve(List<Reservation> competitors);
}
