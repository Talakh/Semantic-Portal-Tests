package semantic.portal.tests.services.api;

import semantic.portal.tests.dto.BranchDto;
import semantic.portal.tests.dto.ConceptDto;
import semantic.portal.tests.dto.DidacticDto;
import semantic.portal.tests.dto.ThesisDTO;

import java.util.List;

public interface BranchApiService {
    BranchDto getAll(String branch);

    List<ThesisDTO> getTheses(String branch);

    List<ConceptDto> getConcepts(String branch);

    List<ThesisDTO> getRelations(String branch);

    List<DidacticDto> getDidactic(String branch);
}
