package com.vl.messenger.domain.usecase

import kotlinx.coroutines.flow.Flow

interface BlockingUseCase<I, O> {
    companion object {
        fun <O> BlockingUseCase<Unit, O>.invoke() = invoke(Unit)
    }

    operator fun invoke(param: I): O
}

interface SuspendedUseCase<I, O> {
    companion object {
        suspend fun <O> SuspendedUseCase<Unit, O>.invoke() = invoke(Unit)
    }

    suspend operator fun invoke(param: I): O
}

interface FlowUseCase<I, O> {
    companion object {
        fun <O> FlowUseCase<Unit, O>.invoke() = invoke(Unit)
    }

    operator fun invoke(param: I): Flow<O>
}