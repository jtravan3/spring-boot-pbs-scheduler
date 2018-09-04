package com.jtravan.pbs.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TestCase {
    private long hchePercentage;
    private long hclePercentage;
    private long lchePercentage;
    private long lclePercentage;
}
