package com.splitpay.transaction;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/tax-rules")
@RequiredArgsConstructor
public class TaxRuleController {

    private final TaxRuleRepository taxRuleRepository;

    @GetMapping
    public List<TaxRule> getAll() {
        return taxRuleRepository.findAll();
    }

    @PostMapping
    public TaxRule save(@RequestBody TaxRule taxRule) {
        return taxRuleRepository.save(taxRule);
    }

    @PutMapping("/{id}")
    public TaxRule update(@PathVariable Long id, @RequestBody TaxRule taxRule) {
        taxRule.setId(id);
        return taxRuleRepository.save(taxRule);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        taxRuleRepository.deleteById(id);
    }
}
