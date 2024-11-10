package com.smu.csd.pokerivals.match;

import com.smu.csd.pokerivals.match.entity.MatchWrapper;
import com.smu.csd.pokerivals.record.Message;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
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

    @PostMapping("/{id}/start")
    @Operation(summary = "Start tournament", description = "can only start a tournament whose registration period has passed")
    public Message startTournament(@PathVariable String id){
        matchService.startSingleEliminationTournament(UUID.fromString(id));
        return new Message("tournament started");
    }

    @PatchMapping("/result")
    public Message setMatchResult(@RequestBody @Valid MatchService.SetMatchResultDTO dto){
        matchService.setMatchResult(dto);
        return new Message("Result set");
    }

    @DeleteMapping("/forfeit")
    @Operation(summary = "Forfeit a player from the match", description = "If forfeitTeamA is false, team A is forfeited o/w team B. You cannot forfeit when that team is not finalised")
    public Message forfeitPlayer(@RequestBody @Valid MatchService.ForfeitDTO dto){
        matchService.forfeit(dto);
        return new Message("Result set");
    }
}
