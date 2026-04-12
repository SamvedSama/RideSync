package com.carpool.frontend.controller;

import com.carpool.frontend.App;
import com.carpool.frontend.util.SessionManager;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;

/**
 * Analytics Controller for ride history and statistics
 * Follows Single Responsibility Principle
 */
public class AnalyticsController {

    @FXML private Label totalRidesLabel;
    @FXML private Label totalEarningsLabel;
    @FXML private Label averageRatingLabel;
    @FXML private Label completionRateLabel;
    @FXML private ComboBox<String> periodComboBox;
    @FXML private BarChart<String, Number> monthlyRidesChart;
    @FXML private PieChart routeDistributionChart;
    @FXML private LineChart<String, Number> earningsTrendChart;
    @FXML private VBox statisticsVBox;

    @FXML
    public void initialize() {
        setupPeriodComboBox();
        loadAnalyticsData();
        setupCharts();
    }
    
    private void setupPeriodComboBox() {
        periodComboBox.getItems().addAll("Last 7 Days", "Last 30 Days", "Last 3 Months", "Last 6 Months", "Last Year");
        periodComboBox.setValue("Last 30 Days");
        
        periodComboBox.setOnAction(event -> loadAnalyticsData());
    }
    
    private void loadAnalyticsData() {
        // In a real application, this would fetch data from the backend
        // For demo purposes, we'll use sample data
        
        String selectedPeriod = periodComboBox.getValue();
        
        // Sample statistics based on period
        switch (selectedPeriod) {
            case "Last 7 Days":
                totalRidesLabel.setText("Total Rides: 12");
                totalEarningsLabel.setText("Total Earnings: $156.00");
                averageRatingLabel.setText("Average Rating: 4.8/5.0");
                completionRateLabel.setText("Completion Rate: 95%");
                break;
            case "Last 30 Days":
                totalRidesLabel.setText("Total Rides: 45");
                totalEarningsLabel.setText("Total Earnings: $585.00");
                averageRatingLabel.setText("Average Rating: 4.7/5.0");
                completionRateLabel.setText("Completion Rate: 92%");
                break;
            case "Last 3 Months":
                totalRidesLabel.setText("Total Rides: 128");
                totalEarningsLabel.setText("Total Earnings: $1,664.00");
                averageRatingLabel.setText("Average Rating: 4.6/5.0");
                completionRateLabel.setText("Completion Rate: 90%");
                break;
            case "Last 6 Months":
                totalRidesLabel.setText("Total Rides: 245");
                totalEarningsLabel.setText("Total Earnings: $3,185.00");
                averageRatingLabel.setText("Average Rating: 4.5/5.0");
                completionRateLabel.setText("Completion Rate: 88%");
                break;
            case "Last Year":
                totalRidesLabel.setText("Total Rides: 512");
                totalEarningsLabel.setText("Total Earnings: $6,656.00");
                averageRatingLabel.setText("Average Rating: 4.4/5.0");
                completionRateLabel.setText("Completion Rate: 85%");
                break;
        }
        
        updateCharts(selectedPeriod);
    }
    
    private void setupCharts() {
        setupMonthlyRidesChart();
        setupRouteDistributionChart();
        setupEarningsTrendChart();
    }
    
    private void setupMonthlyRidesChart() {
        monthlyRidesChart.setTitle("Monthly Ride Statistics");
        monthlyRidesChart.setLegendVisible(false);
        
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Rides per Month");
        
        // Sample data
        series.getData().addAll(
            new XYChart.Data<>("Jan", 15),
            new XYChart.Data<>("Feb", 22),
            new XYChart.Data<>("Mar", 18),
            new XYChart.Data<>("Apr", 25),
            new XYChart.Data<>("May", 30),
            new XYChart.Data<>("Jun", 28)
        );
        
        monthlyRidesChart.getData().add(series);
    }
    
    private void setupRouteDistributionChart() {
        routeDistributionChart.setTitle("Popular Routes");
        
        // Sample data for route distribution
        routeDistributionChart.getData().addAll(
            new PieChart.Data("Main Gate -> Library", 25),
            new PieChart.Data("Library -> Engineering", 20),
            new PieChart.Data("Hostel A -> Main Gate", 18),
            new PieChart.Data("Bus Stop -> Metro Station", 15),
            new PieChart.Data("City Center -> Shopping Mall", 12),
            new PieChart.Data("Other Routes", 10)
        );
    }
    
    private void setupEarningsTrendChart() {
        earningsTrendChart.setTitle("Earnings Trend");
        earningsTrendChart.setLegendVisible(false);
        
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Monthly Earnings");
        
        // Sample earnings data
        series.getData().addAll(
            new XYChart.Data<>("Jan", 195),
            new XYChart.Data<>("Feb", 286),
            new XYChart.Data<>("Mar", 234),
            new XYChart.Data<>("Apr", 325),
            new XYChart.Data<>("May", 390),
            new XYChart.Data<>("Jun", 364)
        );
        
        earningsTrendChart.getData().add(series);
    }
    
    private void updateCharts(String period) {
        // Update charts based on selected period
        // In a real application, this would fetch actual data from the backend
        
        // For demo, we'll just show a message
        System.out.println("Updating charts for period: " + period);
    }
    
    @FXML
    private void exportReport() {
        // In a real application, this would generate and download a PDF/Excel report
        System.out.println("Exporting analytics report...");
        
        // Show confirmation
        totalRidesLabel.setText("Report Generated Successfully!");
        
        // Reset after 3 seconds
        javafx.application.Platform.runLater(() -> {
            try {
                Thread.sleep(3000);
                loadAnalyticsData();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
    }
    
    @FXML
    private void refreshData() {
        loadAnalyticsData();
    }
    
    @FXML
    private void goBack() throws IOException {
        App.setRoot("dashboard");
    }
}
