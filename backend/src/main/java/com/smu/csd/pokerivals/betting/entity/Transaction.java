package com.smu.csd.pokerivals.betting.entity;

import com.fasterxml.jackson.annotation.*;
import com.smu.csd.pokerivals.user.entity.Player;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.ZonedDateTime;

@Entity
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY)
@JsonSubTypes({
        @JsonSubTypes.Type(value = WinBetTransaction.class, name = "win_bet"),
        @JsonSubTypes.Type(value = PlaceBetTransaction.class, name = "place_bet"),
        @JsonSubTypes.Type(value = DepositTransaction.class, name = "deposit")
}
)
public abstract class Transaction {

    protected Transaction(Player player,long cents, ZonedDateTime today){
        this.transactionID = new TransactionID(player,today);
        this.player = player;
        this.changeInCents = cents;
    }

    @Embeddable
    @Getter
    @EqualsAndHashCode
    @NoArgsConstructor
    public static class TransactionID implements Serializable {

        @Column(length=100)
        @Size(max=100)
        private String playerName;

        @Column(nullable = false)
        private ZonedDateTime transactionTime;

        public TransactionID(Player player, ZonedDateTime today){
            this.playerName = player.getUsername();
            this.transactionTime = today;
        }

    }

    @Getter
    @EmbeddedId
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @EqualsAndHashCode.Include
    private TransactionID transactionID;

    @JsonGetter("playerUsername")
    public String getPlayerUsername(){
        return transactionID.getPlayerName();
    }

    @JsonGetter("transactionTime")
    public ZonedDateTime getTransactionTime(){
        return transactionID.getTransactionTime();
    }

    @ManyToOne
    @MapsId("playerName")
    @JsonIgnore
    protected Player player;

    @Getter
    protected long changeInCents;
}
