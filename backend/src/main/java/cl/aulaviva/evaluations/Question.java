package cl.aulaviva.evaluations;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Entity
@Table(name = "questions")
public class Question {

    @Id
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "evaluation_id")
    private Evaluation evaluation;

    private String text;

    private int points;

    private int position;

    private String explanation;

    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("position ASC")
    private List<QuestionOption> options = new ArrayList<>();

    protected Question() {
    }

    public Question(String id, String text, int points, int position, String explanation) {
        this.id = id;
        this.text = text;
        this.points = points;
        this.position = position;
        this.explanation = explanation;
    }

    public void addOption(QuestionOption option) {
        option.setQuestion(this);
        options.add(option);
    }

    /** Alternativa correcta, si existe. */
    public Optional<QuestionOption> correctOption() {
        return options.stream().filter(QuestionOption::isCorrect).findFirst();
    }

    /** Alternativa por indice (0 = primera), si existe. */
    public Optional<QuestionOption> optionAt(int index) {
        return options.stream().filter(o -> o.getPosition() == index).findFirst();
    }

    void setEvaluation(Evaluation evaluation) {
        this.evaluation = evaluation;
    }

    public String getId() {
        return id;
    }

    public Evaluation getEvaluation() {
        return evaluation;
    }

    public String getText() {
        return text;
    }

    public int getPoints() {
        return points;
    }

    public int getPosition() {
        return position;
    }

    public String getExplanation() {
        return explanation;
    }

    public List<QuestionOption> getOptions() {
        return options;
    }
}
