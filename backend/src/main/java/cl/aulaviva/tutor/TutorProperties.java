package cl.aulaviva.tutor;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** Parámetros RAG, caché y presupuesto diario del tutor. */
@ConfigurationProperties(prefix = "aulaviva.tutor")
public class TutorProperties {
    private int topK = 5;
    private double minScore = 2.0;
    private boolean cacheEnabled = true;
    private long dailyTokenBudget = 50000;

    public int topK() { return topK; }
    public double minScore() { return minScore; }
    public boolean cacheEnabled() { return cacheEnabled; }
    public long dailyTokenBudget() { return dailyTokenBudget; }
    public void setTopK(int v) { topK = v; }
    public void setMinScore(double v) { minScore = v; }
    public void setCacheEnabled(boolean v) { cacheEnabled = v; }
    public void setDailyTokenBudget(long v) { dailyTokenBudget = v; }
}
