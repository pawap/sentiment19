package sentiments.ml.classifier;

/**
 * @author Paw
 */
public class Classification
{
    private double probability;

    private boolean offensive;

    public double getProbability() {
        return probability;
    }

    public void setProbability(double probability) {
        this.probability = probability;
    }

    public boolean isOffensive() {
        return offensive;
    }

    public void setOffensive(boolean offensive) {
        this.offensive = offensive;
    }
}
