//package com.fynxt.trading.service;
//
//import com.fynxt.trading.dto.SectorOverlapResponse;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//
//import java.util.Set;
//
//import static org.assertj.core.api.Assertions.assertThat;
//
//@DisplayName("SectorOverlapAnalyzer")
//class SectorOverlapAnalyzerTest {
//
//    private SectorOverlapAnalyzer analyzer;
//
//    @BeforeEach
//    void setUp() {
//        analyzer = new SectorOverlapAnalyzer();
//    }
//
//    // -----------------------------------------------------------------------
//    // computeOverlap formula tests
//    // -----------------------------------------------------------------------
//
//    @Test
//    @DisplayName("Worked example from spec: AAPL+TSLA+NVDA vs TECH_HEAVY = 75%")
//    void computeOverlap_specExample_techHeavy() {
//        Set<String> portfolio = Set.of("AAPL", "TSLA", "NVDA");
//        Set<String> techHeavy = Set.of("AAPL", "MSFT", "GOOGL", "TSLA", "NVDA");
//
//        double overlap = analyzer.computeOverlap(portfolio, techHeavy);
//
//        assertThat(overlap).isEqualTo(75.0);
//    }
//
//    @Test
//    @DisplayName("Worked example from spec: AAPL+TSLA+NVDA vs BALANCED = 50%")
//    void computeOverlap_specExample_balanced() {
//        Set<String> portfolio = Set.of("AAPL", "TSLA", "NVDA");
//        Set<String> balanced = Set.of("AAPL", "JPM", "XOM", "JNJ", "TSLA");
//
//        double overlap = analyzer.computeOverlap(portfolio, balanced);
//
//        assertThat(overlap).isEqualTo(50.0);
//    }
//
//    @Test
//    @DisplayName("Zero overlap when no stocks in common")
//    void computeOverlap_noCommonStocks_returnsZero() {
//        Set<String> portfolio = Set.of("NVDA", "MSFT");
//        Set<String> basket    = Set.of("JPM", "GS", "BAC");
//
//        double overlap = analyzer.computeOverlap(portfolio, basket);
//
//        assertThat(overlap).isEqualTo(0.0);
//    }
//
//    @Test
//    @DisplayName("100% overlap when portfolio equals basket exactly")
//    void computeOverlap_identicalSets_returns100() {
//        Set<String> stocks = Set.of("AAPL", "MSFT");
//
//        double overlap = analyzer.computeOverlap(stocks, stocks);
//
//        assertThat(overlap).isEqualTo(100.0);
//    }
//
//    @Test
//    @DisplayName("Empty portfolio returns 0% overlap")
//    void computeOverlap_emptyPortfolio_returnsZero() {
//        double overlap = analyzer.computeOverlap(Set.of(), Set.of("AAPL", "MSFT"));
//
//        assertThat(overlap).isEqualTo(0.0);
//    }
//
//    // -----------------------------------------------------------------------
//    // analyse() — risk flag and dominant basket
//    // -----------------------------------------------------------------------
//
//    @Test
//    @DisplayName("Full spec worked example → HIGH risk, dominant = TECH_HEAVY")
//    void analyse_specWorkedExample_highRisk() {
//        Set<String> portfolio = Set.of("AAPL", "TSLA", "NVDA");
//
//        SectorOverlapResponse result = analyzer.analyse(portfolio);
//
//        assertThat(result.getRiskFlag()).isEqualTo("HIGH");
//        assertThat(result.getDominantBasket()).isEqualTo("TECH_HEAVY");
//        // 3 baskets must always be returned
//        assertThat(result.getOverlaps()).hasSize(3);
//    }
//
//    @Test
//    @DisplayName("Pure finance portfolio → MEDIUM risk (only FINANCE_HEAVY overlap)")
//    void analyse_financePortfolio_mediumRisk() {
//        // JPM is in both FINANCE_HEAVY (5) and BALANCED (5)
//        // portfolio={JPM,GS,BAC} (3 stocks)
//        // vs FINANCE_HEAVY: common=3 → 2*3/(3+5)*100 = 75% → HIGH actually
//        // Let's use just JPM (1 stock):
//        // vs FINANCE_HEAVY: 2*1/(1+5)*100 = 33.3% → MEDIUM? No, < 40 → LOW
//        // Use JPM + GS (2 stocks):
//        // vs FINANCE_HEAVY: 2*2/(2+5)*100 ≈ 57.14% → MEDIUM
//        Set<String> portfolio = Set.of("JPM", "GS");
//
//        SectorOverlapResponse result = analyzer.analyse(portfolio);
//
//        assertThat(result.getRiskFlag()).isEqualTo("MEDIUM");
//        assertThat(result.getDominantBasket()).isEqualTo("FINANCE_HEAVY");
//    }
//
//    @Test
//    @DisplayName("Niche stock with no basket match → LOW risk")
//    void analyse_nichePortfolio_lowRisk() {
//        Set<String> portfolio = Set.of("XYZ", "ABC");
//
//        SectorOverlapResponse result = analyzer.analyse(portfolio);
//
//        assertThat(result.getRiskFlag()).isEqualTo("LOW");
//        assertThat(result.getOverlaps()).allMatch(o -> o.getOverlap().equals("0.00%"));
//    }
//
//    @Test
//    @DisplayName("Empty portfolio → LOW risk, all overlaps 0%")
//    void analyse_emptyPortfolio_lowRisk() {
//        SectorOverlapResponse result = analyzer.analyse(Set.of());
//
//        assertThat(result.getRiskFlag()).isEqualTo("LOW");
//        assertThat(result.getOverlaps()).allMatch(o -> o.getOverlap().equals("0.00%"));
//    }
//
//    @Test
//    @DisplayName("Overlap formatted to 2 decimal places")
//    void analyse_overlapFormattedCorrectly() {
//        // AAPL+TSLA+NVDA vs TECH_HEAVY = 75.00%
//        Set<String> portfolio = Set.of("AAPL", "TSLA", "NVDA");
//
//        SectorOverlapResponse result = analyzer.analyse(portfolio);
//
//        result.getOverlaps().forEach(o ->
//                assertThat(o.getOverlap()).matches("\\d+\\.\\d{2}%"));
//    }
//}
