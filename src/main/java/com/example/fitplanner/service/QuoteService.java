package com.example.fitplanner.service;

import com.example.fitplanner.dto.QuoteDto;
import com.example.fitplanner.entity.model.Quote;
import com.example.fitplanner.repository.QuoteRepository;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class QuoteService {

    private final QuoteRepository quoteRepository;
    private final ModelMapper modelMapper;

    public QuoteService(QuoteRepository quoteRepository, ModelMapper modelMapper) {
        this.quoteRepository = quoteRepository;
        this.modelMapper = modelMapper;
    }

    public QuoteDto getRandomQuote() {
        return quoteRepository.findRandomQuote()
                .map(quote -> new QuoteDto(
                        quote.getText(),
                        quote.getAuthor()
                ))
                .orElseGet(() ->
                        new QuoteDto(
                                "The only bad workout is the one that didn't happen.",
                                "Unknown"
                        )
                );
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
