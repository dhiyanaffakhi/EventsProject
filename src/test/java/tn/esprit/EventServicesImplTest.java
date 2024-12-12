package tn.esprit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import tn.esprit.eventsproject.entities.Event;
import tn.esprit.eventsproject.entities.Logistics;
import tn.esprit.eventsproject.entities.Participant;
import tn.esprit.eventsproject.entities.Tache;
import tn.esprit.eventsproject.repositories.EventRepository;
import tn.esprit.eventsproject.repositories.LogisticsRepository;
import tn.esprit.eventsproject.repositories.ParticipantRepository;
import tn.esprit.eventsproject.services.EventServicesImpl;


import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EventServicesImplTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private ParticipantRepository participantRepository;

    @Mock
    private LogisticsRepository logisticsRepository;

    @InjectMocks
    private EventServicesImpl eventServices;

    private Participant participant;
    private Event event;
    private Logistics logistics;

    @BeforeEach
    public void setUp() {
        // Initialize test data
        participant = new Participant();
        participant.setIdPart(1);
        participant.setNom("Tounsi");
        participant.setPrenom("Ahmed");

        event = new Event();
        event.setDescription("Test Event");
        
        logistics = new Logistics();
        logistics.setPrixUnit(10.0f);
        logistics.setQuantite(5);
        logistics.setReserve(true);
    }

    @Test
    public void testAddParticipant() {
        when(participantRepository.save(participant)).thenReturn(participant);

        Participant savedParticipant = eventServices.addParticipant(participant);

        assertNotNull(savedParticipant);
        verify(participantRepository).save(participant);
    }

    @Test
    public void testAddAffectEvenParticipantWithSingleParticipant() {
        when(participantRepository.findById(1)).thenReturn(Optional.of(participant));
        when(eventRepository.save(event)).thenReturn(event);

        Event savedEvent = eventServices.addAffectEvenParticipant(event, 1);

        assertNotNull(savedEvent);
        assertTrue(participant.getEvents().contains(event));
        verify(eventRepository).save(event);
    }

    @Test
    public void testAddAffectEvenParticipantWithMultipleParticipants() {
        Set<Participant> participants = new HashSet<>();
        participants.add(participant);
        event.setParticipants(participants);

        Participant anotherParticipant = new Participant();
        anotherParticipant.setIdPart(2);
        participants.add(anotherParticipant);

        when(participantRepository.findById(1)).thenReturn(Optional.of(participant));
        when(participantRepository.findById(2)).thenReturn(Optional.of(anotherParticipant));
        when(eventRepository.save(event)).thenReturn(event);

        Event savedEvent = eventServices.addAffectEvenParticipant(event);

        assertNotNull(savedEvent);
        assertTrue(participant.getEvents().contains(event));
        assertTrue(anotherParticipant.getEvents().contains(event));
        verify(eventRepository).save(event);
    }

    @Test
    public void testAddAffectLog() {
        when(eventRepository.findByDescription("Test Event")).thenReturn(event);
        when(logisticsRepository.save(logistics)).thenReturn(logistics);

        Logistics savedLogistics = eventServices.addAffectLog(logistics, "Test Event");

        assertNotNull(savedLogistics);
        assertTrue(event.getLogistics().contains(logistics));
        verify(logisticsRepository).save(logistics);
    }

    @Test
    public void testGetLogisticsDates() {
        LocalDate startDate = LocalDate.of(2024, 1, 1);
        LocalDate endDate = LocalDate.of(2024, 12, 31);

        event.setLogistics(new HashSet<>(Arrays.asList(logistics)));

        when(eventRepository.findByDateDebutBetween(startDate, endDate))
            .thenReturn(Arrays.asList(event));

        List<Logistics> result = eventServices.getLogisticsDates(startDate, endDate);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(logistics, result.get(0));
    }

    @Test
    public void testCalculCout() {
        participant.setTache(Tache.ORGANISATEUR);
        event.setParticipants(new HashSet<>(Arrays.asList(participant)));
        
        Set<Logistics> logisticsSet = new HashSet<>();
        logisticsSet.add(logistics);
        event.setLogistics(logisticsSet);

        when(eventRepository.findByParticipants_NomAndParticipants_PrenomAndParticipants_Tache(
            "Tounsi", "Ahmed", Tache.ORGANISATEUR))
            .thenReturn(Arrays.asList(event));

        eventServices.calculCout();

        verify(eventRepository).save(event);
        assertEquals(50.0f, event.getCout(), 0.001f);
    }

    @Test
    public void testGetLogisticsDatesWithNoLogistics() {
        LocalDate startDate = LocalDate.of(2024, 1, 1);
        LocalDate endDate = LocalDate.of(2024, 12, 31);

        event.setLogistics(new HashSet<>());

        when(eventRepository.findByDateDebutBetween(startDate, endDate))
            .thenReturn(Arrays.asList(event));

        List<Logistics> result = eventServices.getLogisticsDates(startDate, endDate);

        assertNull(result);
    }
}
