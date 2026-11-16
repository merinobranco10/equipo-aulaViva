package cl.aulaviva.evaluations;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "options")
public class QuestionOption {

    @Id
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id")
    private Question question;

    private String text;

    private int position;

    @Column(name = "is_correct")
    private boolean correct;

    protected QuestionOption() {
    }

    public QuestionOption(String id, String text, int position, boolean correct) {
        this.id = id;
        this.text = text;
        this.position = position;
        this.correct = correct;
    }

    void setQuestion(Question question) {
        this.question = question;
    }

    public String getId() {
        return id;
    }

    public String getText() {
        return text;
    }

    public int getPosition() {
        return position;
    }

    public boolean isCorrect() {
        return correct;
    }
}
