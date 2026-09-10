package com.labresa.patterns.strategy;

//import java.util.Comparator;
import java.util.List;

import com.labresa.model.Reservation;

public interface PriorityStrategy {

    Reservation resolve(List<Reservation> competitors);
}
