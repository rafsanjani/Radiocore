package com.radiocore.news.data

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import timber.log.Timber

@ExperimentalCoroutinesApi

internal fun <RESULT, REQUEST> networkBoundedFlow(
        local: Flow<RESULT>,
        save: suspend (response: REQUEST) -> Unit,
        remote: suspend () -> REQUEST,
        log: (e: Throwable) -> Unit = {
            Timber.e(it)
        }
): Flow<RESULT> = flow {
    try {
        local.first()?.let {
            emit(it)
        }

        remote()?.let {
            save(it)
        }
    } catch (e: Throwable) {
        log(e)
    }
    emitAll(local.filter { it != null }.map {
        it
    })
}

@ExperimentalCoroutinesApi
fun <T> Flow<T>.handleErrors(): Flow<T> =
        catch { e ->
            Timber.e(e)
        }