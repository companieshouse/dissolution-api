package uk.gov.companieshouse.controller;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.companieshouse.api.model.payment.Cost;
import uk.gov.companieshouse.exception.DissolutionNotFoundException;
import uk.gov.companieshouse.service.cost.CostService;

import java.util.Collections;


@RestController
@RequestMapping("/transactions/{transaction_id}/dissolution/{dissolution_id}/costs")
public class CostController {
    private final CostService costService;

    public CostController(CostService costService) {
        this.costService = costService;
    }

    @GetMapping
    public ResponseEntity<Object> getCosts(
            @PathVariable("transaction_id") String transactionId,
            @PathVariable("dissolution_id") String dissolutionId) throws DissolutionNotFoundException {
        // transaction_id is authorised upstream by the Transaction API.
        Cost cost = costService.getCosts(dissolutionId);
        return ResponseEntity.ok(Collections.singletonList(cost));
    }
}
