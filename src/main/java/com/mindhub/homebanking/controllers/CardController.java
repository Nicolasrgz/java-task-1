package com.mindhub.homebanking.controllers;

import com.mindhub.homebanking.models.*;
import com.mindhub.homebanking.repositories.AccountRepository;
import com.mindhub.homebanking.repositories.CardRepository;
import com.mindhub.homebanking.repositories.ClientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api")
public class CardController {

    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private ClientRepository clientRepository;
    @Autowired
    private CardRepository cardRepository;


    @PostMapping("/clients/current/cards")
    public ResponseEntity<Object> createCards(
            @RequestParam CardColor color,
            @RequestParam CardType type,
            Authentication authentication) {

        String cardNumber;
        long cvvNumber = (long) ((Math.random() * (999 - 100)) + 100);

        Client client = clientRepository.findByEmail(authentication.getName());

        if (client.getCards().size() >= 6) {
            return new ResponseEntity<>("Client already has 6 cards registered", HttpStatus.FORBIDDEN);
        }

        if (cardRepository.countByTypeAndClient(type, client) >= 3) {
            return new ResponseEntity<>("The client already has 3 registered " + type + " cards.", HttpStatus.FORBIDDEN);
        }

        if (cardRepository.countByColorAndTypeAndClient(color, type, client) >= 1) {
            return new ResponseEntity<>("The client already has a " + color + " " + type + " card.", HttpStatus.FORBIDDEN);
        }

        do {
            cardNumber = String.format("%04d-%04d-%04d-%04d", (int)(Math.random()*10000), (int)(Math.random()*10000), (int)(Math.random()*10000), (int)(Math.random()*10000));
        } while (cardRepository.findByNumber(cardNumber) != null);

        Card card = new Card(type, color, client.getFirstName()+ " " + client.getLastName(), cardNumber, cvvNumber, LocalDateTime.now().plusYears(5), LocalDateTime.now());
        client.addCards(card);
        cardRepository.save(card);

        return new ResponseEntity<>(HttpStatus.CREATED);
    }

}
