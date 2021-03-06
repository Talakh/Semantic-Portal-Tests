package semantic.portal.tests.services.tests.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import semantic.portal.tests.dto.ConceptDto;
import semantic.portal.tests.dto.ThesisDTO;
import semantic.portal.tests.enums.TestTypeEnum;
import semantic.portal.tests.model.Answer;
import semantic.portal.tests.model.DomainUrl;
import semantic.portal.tests.model.Test;
import semantic.portal.tests.model.TestDifficultLevel;
import semantic.portal.tests.services.tests.SPTest;

import java.util.*;

import static java.util.stream.Collectors.*;
import static semantic.portal.tests.utils.TestUtils.getRandomConcept;

@Service
public class SeveralCorrectAnswersTestImpl implements SPTest {
    private static final String QUESTION_TEMPLATE = "Choose the statements applicable to the concept \"%s\"?";
    private static final int ANSWERS_COUNT = 4;

    @Override
    public Test create(List<ConceptDto> concepts, List<ThesisDTO> theses, List<String> thesesTypesForAnswer) {
        Map<Integer, List<ThesisDTO>> conceptsWithSeveralAnswers = filterConceptsWithSeveralAnswers(theses,
                thesesTypesForAnswer);
        Map<Integer, ConceptDto> possibleConceptsForTest = filterPossibleConcepts(concepts,
                conceptsWithSeveralAnswers.keySet());
        ConceptDto question = getRandomConcept(possibleConceptsForTest);

        return Test.builder()
                .domainUrl(Collections.singleton(question.getDomain()))
                .domainName(Collections.singleton(question.getConcept()))
                .question(String.format(QUESTION_TEMPLATE, question.getConcept()))
                .answers(getAnswers(question, conceptsWithSeveralAnswers, possibleConceptsForTest, theses))
                .type(TestTypeEnum.SEVERAL_CORRECT_ANSWER)
                .build();
    }

    @Override
    public boolean isEnoughTheses(List<ConceptDto> concepts,
                                  List<ThesisDTO> theses,
                                  TestDifficultLevel level) {
        if (level.getSeveralAnswerCount() <= 0) {
            return true;
        } else {
            Map<Integer, List<ThesisDTO>> conceptsWithSeveralAnswers =
                    filterConceptsWithSeveralAnswers(theses, level.getSeveralAnswerThesisTypes());
            return conceptsWithSeveralAnswers.values()
                    .stream()
                    .mapToLong(Collection::size)
                    .sum() >= 4;
        }
    }

    private Map<Integer, List<ThesisDTO>> filterConceptsWithSeveralAnswers(List<ThesisDTO> theses,
                                                                           List<String> thesesTypesForAnswer) {
        return theses.stream()
                .filter(t -> Objects.isNull(t.getToThesisId()))
                .filter(t -> thesesTypesForAnswer.contains(t.getClazz()))
                .collect(groupingBy(ThesisDTO::getConceptId))
                .entrySet().stream()
                .filter(e -> !e.getValue().isEmpty() && e.getValue().size() <= ANSWERS_COUNT)
                .collect(toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private Map<Integer, ConceptDto> filterPossibleConcepts(List<ConceptDto> concepts, Set<Integer> possibleConcepts) {
        return concepts.stream()
                .filter(conceptDto -> conceptDto.getIsAspect() == 0)
                .filter(concept -> possibleConcepts.contains(concept.getId()))
                .collect(toMap(ConceptDto::getId, c -> c));
    }

    private List<Answer> getAnswers(ConceptDto question,
                                    Map<Integer, List<ThesisDTO>> conceptsWithSeveralAnswers,
                                    Map<Integer, ConceptDto> possibleConceptForTest,
                                    List<ThesisDTO> theses) {
        List<Answer> answers = new ArrayList<>();
        conceptsWithSeveralAnswers.get(question.getId())
                .forEach(thesis -> answers.add(Answer.createAnswer(thesis.getThesis(), Boolean.TRUE)));
        answers.addAll(generateWrongAnswers(question, conceptsWithSeveralAnswers, possibleConceptForTest, theses,
                ANSWERS_COUNT - answers.size()));
        Collections.shuffle(answers);
        return answers;
    }

    private List<Answer> generateWrongAnswers(ConceptDto question,
                                              Map<Integer, List<ThesisDTO>> conceptsWithSeveralAnswers,
                                              Map<Integer, ConceptDto> possibleConceptForTest,
                                              List<ThesisDTO> theses,
                                              int count) {
        List<Answer> answers = possibleConceptForTest.keySet().stream()
                .filter(id -> id != question.getId())
                .map(conceptsWithSeveralAnswers::get)
                .flatMap(Collection::stream)
                .map(ThesisDTO::getThesis)
                .map(answer -> Answer.createAnswer(answer, Boolean.FALSE))
                .collect(toList());

        if (answers.size() < count) {
            theses.stream()
                    .filter(t -> t.getConceptId() != question.getId())
                    .map(t -> Answer.createAnswer(t.getThesis(), Boolean.FALSE))
                    .limit(count - answers.size())
                    .forEach(answers::add);
            Collections.shuffle(answers);
        } else {
            Collections.shuffle(answers);
            answers = answers.subList(0, count);
        }
        return answers;
    }
}
