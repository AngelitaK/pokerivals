package com.smu.csd.pokerivals.betting.entity;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.smu.csd.pokerivals.user.entity.Player;
import com.stripe.model.checkout.Session;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.validation.constraints.Positive;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

@Entity
@NoArgsConstructor
public class DepositTransaction extends Transaction{

    /**
     * Upon creation, it is an empty deposit while waiting for session to complete
     *
     * @param stripeSession session
     * @param player        player
     * @param today         today
     */
    public DepositTransaction(Session stripeSession, Player player, ZonedDateTime today){
        super(player,0, today);
        this.stripeCheckoutSessionId = stripeSession.getId();
    }

    @Column(unique = true)
    private String stripeCheckoutSessionId;

    /**
     * Will check whether email matches, same checkout session ID store, and is complete
     *
     * @param email email of user
     * @param checkoutSessionId  checkout session ID to match
     * @param checkoutStatus  the current checkout sesion status
     */
    public void confirm(String email, String checkoutSessionId, String checkoutStatus, @Positive long amountTotal){
        if (!email.equals(player.getEmail())
                || !checkoutSessionId.equals(stripeCheckoutSessionId)
                || !checkoutStatus.equals("complete")
        ){
            throw new IllegalArgumentException("stripeSession does not correspond to this transaction");
        }
        this.changeInCents = amountTotal;
    }

    @JsonGetter("pending")
    public boolean isPending(){
        return changeInCents == 0;
    }
}
