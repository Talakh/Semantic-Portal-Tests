package semantic.portal.tests.services.tests.impl;

import com.google.common.collect.Lists;
import org.springframework.stereotype.Service;
import semantic.portal.tests.dto.ConceptDto;
import semantic.portal.tests.dto.ThesisDTO;
import semantic.portal.tests.model.Answer;
import semantic.portal.tests.model.Test;
import semantic.portal.tests.services.tests.SPTest;

import java.util.*;
import java.util.stream.Collectors;

import static semantic.portal.tests.enums.ThesesClassEnum.ESSENCE;

@Service
public class OneCorrectAnswerTestImpl implements SPTest {
    private static final int ANSWERS_COUNT = 4;
    private static final List<String> thesesTypesForAnswer = Lists.newArrayList(ESSENCE.getValue());
    private static final Random random = new Random();

    @Override
    public Test create(List<ConceptDto> concepts, List<ThesisDTO> theses) {
        Map<String, List<ConceptDto>> possibleDomainsForTest = filterPossibleDomains(concepts);
        String domainForTest = getDomainForTest(possibleDomainsForTest);
        List<ConceptDto> domainConceptsForTest = possibleDomainsForTest.get(domainForTest);

        ConceptDto question = getRandomConcept(domainConceptsForTest);

        return Test.builder()
                .question(question.getConcept())
                .answers(getAnswers(question, theses, domainConceptsForTest))
                .build();
    }

    private Map<String, List<ConceptDto>> filterPossibleDomains(List<ConceptDto> concepts) {
        return concepts.stream()
                .collect(Collectors.groupingBy(ConceptDto::getDomain))
                .entrySet().stream()
                .filter(entry -> entry.getValue().size() >= ANSWERS_COUNT)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private String getDomainForTest(Map<String, List<ConceptDto>> domainsMap) {
        return new ArrayList<>(domainsMap.keySet()).get(random.nextInt(domainsMap.size()));
    }

    private ConceptDto getRandomConcept(List<ConceptDto> concepts) {
        ConceptDto concept = concepts.get(random.nextInt(concepts.size()));
        concepts.remove(concept);
        return concept;
    }

    private List<Answer> getAnswers(ConceptDto question, List<ThesisDTO> theses, List<ConceptDto> concepts) {
        List<Answer> answers = new ArrayList<>();
        answers.add(Answer.createAnswer(getConceptThesis(question.getId(), theses), Boolean.TRUE));
        answers.addAll(generateWrongAnswers(concepts, theses));
        Collections.shuffle(answers);
        return answers;
    }

    private List<Answer> generateWrongAnswers(List<ConceptDto> concepts, List<ThesisDTO> thesis) {
        List<Integer> conceptsIds = concepts.stream().map(ConceptDto::getId).collect(Collectors.toList());
        List<Answer> answers = thesis.stream()
                .filter(t -> conceptsIds.contains(t.getConceptId()))
                .filter(t -> thesesTypesForAnswer.contains(t.getClazz()))
                .map(ThesisDTO::getThesis)
                .map(answer -> Answer.createAnswer(answer, Boolean.FALSE))
                .collect(Collectors.toList());
        Collections.shuffle(answers);

        return answers.subList(0, ANSWERS_COUNT - 1);
    }

    private String getConceptThesis(int conceptId, List<ThesisDTO> theses) {
        return theses.stream()
                .filter(thesis -> thesis.getConceptId() == conceptId)
                .filter(thesis -> thesesTypesForAnswer.contains(thesis.getClazz()))
                .map(ThesisDTO::getThesis)
                .findAny()
                .orElseThrow(NullPointerException::new);
    }

}