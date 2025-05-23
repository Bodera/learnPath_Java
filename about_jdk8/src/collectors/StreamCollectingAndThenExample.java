package collectors;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class StreamCollectingAndThenExample {

    public static void main(String[] args) {
        
        PortfolioSummary summary = getSummary();
        System.out.println(summary);

        List<Stock> filteredStocks = getFilteredStocks(50.0);
        System.out.println(filteredStocks);

        Map<String, List<Stock>> stocksBySymbol = getStocksBySymbol();
        System.out.println(stocksBySymbol);
    }

    private static List<Bond> getBonds() {
        List<Bond> bonds = new ArrayList<>();
        bonds.add(new Bond("US Treasury 10yr", 1000.0, 0.05));
        bonds.add(new Bond("German Bund 10yr", 500.0, 0.03));
        bonds.add(new Bond("Japanese Govt 10yr", 2000.0, 0.02));
        bonds.add(new Bond("UK Gilt 10yr", 1500.0, 0.04));
        bonds.add(new Bond("Australian Govt 10yr", 2500.0, 0.06));
        return bonds;
    }

    private static List<Stock> getStocks() {
        List<Stock> stocks = new ArrayList<>();
        stocks.add(new Stock("Apple", 100.0, 5));
        stocks.add(new Stock("Microsoft", 50.0, 10));
        stocks.add(new Stock("Google", 75.0, 8));
        stocks.add(new Stock("Amazon", 80.0, 6));
        stocks.add(new Stock("Tesla", 120.0, 4));
        return stocks;
    }

    /**
     * Calculates and returns a summary of the total value of a portfolio that consists of stocks and bonds.
     * The total value is the sum of the total value of all stocks and bonds. The total value of the stocks
     * is the sum of the price of each stock multiplied by its quantity. The total value of the bonds is
     * the sum of the face value of each bond multiplied by its interest rate.
     * 
     * @return a PortfolioSummary that contains the total value of the portfolio, the total value of the
     *         stocks, and the total value of the bonds
     */
    private static PortfolioSummary getSummary() {
        List<Stock> stocks = getStocks();
        List<Bond> bonds = getBonds();

        double totalStockValue = stocks.stream()
                .collect(Collectors.collectingAndThen(
                        Collectors.summingDouble(Stock::getPrice),
                        total -> total * stocks.stream().mapToInt(Stock::getQuantity).sum()));

        double totalBondValue = bonds.stream()
                .collect(Collectors.collectingAndThen(
                        Collectors.summingDouble(Bond::getFaceValue),
                        total -> total * bonds.stream().mapToDouble(Bond::getInterestRate).sum()));

        double totalValue = totalStockValue + totalBondValue;

        return new PortfolioSummary(totalValue, totalStockValue, totalBondValue);
    }

/**
 * Filters and returns a list of stocks with a price greater than the specified threshold.
 * The resulting list is sorted in descending order by price.
 * 
 * @param threshold the price threshold for filtering stocks
 * @return a list of stocks with a price greater than the threshold, sorted in descending order
 */

    private static List<Stock> getFilteredStocks(double threshold) {
        return getStocks().stream()
                .collect(Collectors.collectingAndThen(
                        Collectors.filtering(stock -> stock.getPrice() > threshold, Collectors.toList()),
                        filteredStocks -> filteredStocks.stream()
                                .sorted(Comparator.comparing(Stock::getPrice).reversed())
                                .collect(Collectors.toList())));
    }

    /**
     * Returns a map of stock symbols to a list of {@link Stock}s sorted in descending order of price.
     * The map is sorted by symbol in ascending order.
     * @return a map of stock symbols to a list of {@link Stock}s
     */
    private static Map<String, List<Stock>> getStocksBySymbol() {
        return getStocks().stream()
                .collect(Collectors.collectingAndThen(
                        Collectors.groupingBy(Stock::getSymbol),
                        groupedStocks -> groupedStocks.entrySet().stream()
                                .collect(Collectors.toMap(
                                        Map.Entry::getKey,
                                        entry -> entry.getValue().stream()
                                                .sorted(Comparator.comparing(Stock::getPrice).reversed())
                                                .collect(Collectors.toList())))));
    }

    static class PortfolioSummary {
        private double totalValue;
        private double stockValue;
        private double bondValue;

        public PortfolioSummary(double totalValue, double stockValue, double bondValue) {
            this.totalValue = totalValue;
            this.stockValue = stockValue;
            this.bondValue = bondValue;
        }

        public double getTotalValue() {
            return totalValue;
        }

        public double getStockValue() {
            return stockValue;
        }

        public double getBondValue() {
            return bondValue;
        }

        @Override
        public String toString() {
            return "Portfolio Summary:\n" +
                    "Total Value: $" + totalValue + "\n" +
                    "Stock Value: $" + stockValue + "\n" +
                    "Bond Value: $" + bondValue + "\n";
        }
    }

    /**
     * Represents a stock investment.
     * 
     * A stock, also known as equity, represents ownership in a company. When you
     * buy a stock, you are essentially buying a small portion of that company's
     * assets and profits. Stocks can be traded on public markets, and their value
     * can fluctuate based on various market and economic factors.
     * 
     */
    static class Stock {
        private String symbol;
        private double price;
        private int quantity;

        public Stock(String symbol, double price, int quantity) {
            this.symbol = symbol;
            this.price = price;
            this.quantity = quantity;
        }

        public String getSymbol() {
            return symbol;
        }

        public double getPrice() {
            return price;
        }

        public int getQuantity() {
            return quantity;
        }

        @Override
        public String toString() {
            return "Stock: " + symbol + ", Price: $" + price + ", Quantity: " + quantity;
        }
    }

    /**
     * Represents a bond investment.
     * 
     * A bond is a type of investment where an investor loans money to a borrower
     * (typically a corporation or government entity) in exchange for regular
     * interest payments and the eventual return of their principal investment.
     * Bonds are often used to finance large projects or to raise capital for
     * businesses.
     * 
     */
    static class Bond {
        private String name;
        private double faceValue;
        private double interestRate;

        public Bond(String name, double faceValue, double interestRate) {
            this.name = name;
            this.faceValue = faceValue;
            this.interestRate = interestRate;
        }

        public String getName() {
            return name;
        }

        public double getFaceValue() {
            return faceValue;
        }

        public double getInterestRate() {
            return interestRate;
        }

        @Override
        public String toString() {
            return "Bond: " + name + ", Face Value: $" + faceValue + ", Interest Rate: " + interestRate + "%";
        }
    }

    /**
     * Represents a collection of investments, including stocks and bonds.
     * 
     * A portfolio is a container that holds multiple investments, allowing you to
     * manage and track their performance as a whole.
     * 
     */
    static class Portfolio {
        private List<Stock> stocks;
        private List<Bond> bonds;

        public Portfolio() {
            this.stocks = new ArrayList<>();
            this.bonds = new ArrayList<>();
        }

        public void addStock(Stock stock) {
            stocks.add(stock);
        }

        public void addBond(Bond bond) {
            bonds.add(bond);
        }

        public List<Stock> getStocks() {
            return stocks;
        }

        public List<Bond> getBonds() {
            return bonds;
        }

        @Override
        public String toString() {
            return "Portfolio:\n" +
                    "Stocks:\n" + stocks + "\n" +
                    "Bonds:\n" + bonds + "\n";
        }
    }
}
