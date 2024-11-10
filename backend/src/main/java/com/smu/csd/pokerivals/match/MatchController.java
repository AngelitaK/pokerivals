package com.smu.csd.pokerivals.match;

import com.smu.csd.pokerivals.match.entity.MatchWrapper;
import com.smu.csd.pokerivals.record.Message;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/tournament/match")
@CrossOrigin
public class MatchController {

    @Autowired
    public MatchController(MatchService matchService) {
        this.matchService = matchService;
    }

    private MatchService matchService;

    @GetMapping("/{id}")
    @Operation(summary = "get all matches of a given tournament to display", description = "list of lists. the outer list ordered from the last round to the first round")
    public List<MatchWrapper.MatchRoundDTO> fetchMyTournaments(@PathVariable String id){
        return matchService.generateFrontendFriendlyBrackets(UUID.fromString(id));
    }

    public Message startTournament(){
        return new Message("tournament started");
    }

    public Message setMatchResult(){
        return new Message("Result set");
    }

    public Message forfeitPlayer(){
        return new Message("Result set");
    }
}
