package com.example.fitplanner.service;

import com.example.fitplanner.dto.QuoteDto;
import com.example.fitplanner.entity.model.Quote;
import com.example.fitplanner.repository.QuoteRepository;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
public class QuoteService {

    private final QuoteRepository quoteRepository;
    private final ModelMapper modelMapper;

    public QuoteService(QuoteRepository quoteRepository, ModelMapper modelMapper) {
        this.quoteRepository = quoteRepository;
        this.modelMapper = modelMapper;
    }

    public QuoteDto getRandomQuote() {
        long count = quoteRepository.count();

        // Fallback if the database is empty
        if (count == 0) {
            return new QuoteDto("The only bad workout is the one that didn't happen.", "Unknown");
        }

        // Generate a random index based on the count of quotes in DB
        int index = new Random().nextInt((int) count);

        // Use Pageable to fetch exactly one record at that random offset
        Page<Quote> quotePage = quoteRepository.findAll(PageRequest.of(index, 1));

        if (quotePage.hasContent()) {
            Quote quote = quotePage.getContent().get(0);
            return new QuoteDto(quote.getText(), quote.getAuthor());
        }

        return new QuoteDto("Keep pushing forward.", "FitPlanner");
    }

    public List<QuoteDto> getAll() {
        List<Quote> quotes = quoteRepository.findAll();
        List<QuoteDto> dtos = new ArrayList<>();
        for (Quote quote : quotes) {
            dtos.add(modelMapper.map(quote, QuoteDto.class));
        }
        return dtos;
    }

    public void delete(Long id) {
        Quote quote = quoteRepository.findById(id).orElseThrow();
        quoteRepository.delete(quote);
    }

    public void create(QuoteDto quoteDto) {
        Quote quote = new Quote();
        quote.setText(quoteDto.getText());
        quote.setAuthor(quoteDto.getAuthor());

        quoteRepository.save(quote);
    }

    public void update(Long id, QuoteDto quoteDto) {
        Quote existingQuote = quoteRepository.findById(id)
                .orElseThrow();

        existingQuote.setText(quoteDto.getText());
        existingQuote.setAuthor(quoteDto.getAuthor());

        quoteRepository.save(existingQuote);
    }
}
