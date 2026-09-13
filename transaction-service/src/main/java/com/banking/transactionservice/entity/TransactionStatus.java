package com.banking.transactionservice.entity;

/*

    Transaciton lifecycle flow

    pending -> processing -> completed = clean transaciton

    pending -> processing -> Pending verification (suspecious detected) -> verify-> Completed
                                                                        -> notVerify -> Flagged-> SAGA(refund)

*/

public enum TransactionStatus {
    PENDING,
    PROCESSING,
    PENDING_VERIFICATION,
    COMPLETED,
    FAILED,
    FLAGGED

}