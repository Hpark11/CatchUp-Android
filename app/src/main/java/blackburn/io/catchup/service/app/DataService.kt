package blackburn.io.catchup.service.app

import blackburn.io.catchup.app.Define
import blackburn.io.catchup.di.scope.AppScope
import com.amazonaws.amplify.generated.graphql.*
import com.amazonaws.mobileconnectors.appsync.AWSAppSyncClient
import com.amazonaws.mobileconnectors.appsync.AppSyncSubscriptionCall
import com.amazonaws.mobileconnectors.appsync.fetcher.AppSyncResponseFetchers
import com.apollographql.apollo.GraphQLCall
import com.apollographql.apollo.api.*
import com.apollographql.apollo.exception.ApolloException
import com.apollographql.apollo.fetcher.ResponseFetcher
import com.apollographql.apollo.internal.util.Cancelable
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import javax.inject.Inject
import io.reactivex.ObservableEmitter
import io.reactivex.FlowableEmitter
import io.reactivex.Flowable
import io.reactivex.BackpressureStrategy
import type.CatchUpPromiseInput
import type.CatchUpUserInput
import type.ContactUpdateInput


@AppScope
class DataService @Inject constructor(private val client: AWSAppSyncClient) {

  private fun <D : Operation.Data, T, V : Operation.Variables> from(
    operation: Operation<D, T, V>,
    fetcher: ResponseFetcher = AppSyncResponseFetchers.CACHE_AND_NETWORK
  ): Observable<Response<T>> {

    return Observable.create { emitter ->
      val call: GraphQLCall<T>
      when (operation) {
        is Query<D, T, V> -> call = client.query(operation).responseFetcher(fetcher)
        is Mutation<D, T, V> -> call = client.mutate(operation)
        else -> throw Exception("Exception message")
      }

      cancelOnDisposed(emitter, call)

      call.enqueue(object : GraphQLCall.Callback<T>() {
        override fun onResponse(response: Response<T>) {
          if (!emitter.isDisposed) emitter.onNext(response)
        }

        override fun onFailure(e: ApolloException) {
          if (!emitter.isDisposed) emitter.onError(e)
        }

        override fun onStatusEvent(event: GraphQLCall.StatusEvent) {
          if (event == GraphQLCall.StatusEvent.COMPLETED && !emitter.isDisposed) {
            emitter.onComplete()
          }
        }
      })
    }
  }

  private fun <D : Operation.Data, T, V : Operation.Variables> from(
    subscription: Subscription<D, T, V>
  ): Flowable<Response<T>> {
    return from(subscription, BackpressureStrategy.LATEST)
  }

  private fun <D : Operation.Data, T, V : Operation.Variables> from(
    subscription: Subscription<D, T, V>,
    backPressureStrategy: BackpressureStrategy
  ): Flowable<Response<T>> {

    return Flowable.create({ emitter ->
      val call = client.subscribe(subscription)
      cancelOnDisposed(emitter, call)

      call.execute(object : AppSyncSubscriptionCall.Callback<T> {
        override fun onResponse(response: Response<T>) {
          if (!emitter.isCancelled) emitter.onNext(response)
        }

        override fun onFailure(e: ApolloException) {
          if (!emitter.isCancelled) emitter.onError(e)
        }

        override fun onCompleted() {
          if (!emitter.isCancelled) emitter.onComplete()
        }
      })
    }, backPressureStrategy)
  }

  private fun <T> cancelOnDisposed(emitter: FlowableEmitter<T>, cancelable: Cancelable) {
    emitter.setDisposable(disposable(cancelable))
  }

  private fun <T> cancelOnDisposed(emitter: ObservableEmitter<T>, cancelable: Cancelable) {
    emitter.setDisposable(disposable(cancelable))
  }

  private fun disposable(cancelable: Cancelable): Disposable {
    return object : Disposable {
      override fun dispose() {
        cancelable.cancel()
      }

      override fun isDisposed() = cancelable.isCanceled
    }
  }

  fun requestAppVersion(): Observable<Response<CheckAppVersionQuery.Data>> {
    return from(
      CheckAppVersionQuery.builder().platform(Define.PLATFORM_ANDROID).build(),
      AppSyncResponseFetchers.NETWORK_ONLY
    )
  }

  fun requestUser(id: String): Observable<Response<GetCatchUpUserQuery.Data>> {
    return from(GetCatchUpUserQuery.builder().id(id).build())
  }

  fun requestContacts(ids: List<String>): Observable<Response<BatchGetCatchUpContactsQuery.Data>> {
    return from(BatchGetCatchUpContactsQuery.builder().ids(ids).build())
  }

  fun requestPromise(id: String): Observable<Response<GetCatchUpPromiseQuery.Data>> {
    return from(GetCatchUpPromiseQuery.builder().id(id).build())
  }

  fun updateUser(
    id: String,
    phone: String?,
    email: String?,
    nickname: String?,
    profileImagePath: String?,
    gender: String?,
    birthday: String?,
    ageRange: String?,
    credit: Int?
  ): Observable<Response<UpdateCatchUpUserMutation.Data>> {
    return from(
      UpdateCatchUpUserMutation.builder().id(id).data(
        CatchUpUserInput.builder()
          .phone(phone)
          .email(email)
          .nickname(nickname)
          .profileImagePath(profileImagePath)
          .ageRange(ageRange)
          .gender(gender)
          .birthday(birthday)
          .credit(credit)
          .build())
        .build())
  }

  fun createUser(
    id: String,
    phone: String?,
    email: String?,
    nickname: String?,
    profileImagePath: String?,
    gender: String?,
    birthday: String?,
    ageRange: String?
  ): Observable<Response<CreateCatchUpUserMutation.Data>> {
    return from(
      CreateCatchUpUserMutation.builder().id(id).data(
        CatchUpUserInput.builder()
          .phone(phone)
          .email(email)
          .nickname(nickname)
          .profileImagePath(profileImagePath)
          .ageRange(ageRange)
          .gender(gender)
          .birthday(birthday)
          .credit(0)
          .build())
        .build())
  }

  fun updateContact(
    phone: String,
    nickname: String?,
    profileImagePath: String?,
    pushToken: String?,
    osType: String?
  ): Observable<Response<UpdateCatchUpContactMutation.Data>> {
    return from(
      UpdateCatchUpContactMutation.builder().phone(phone).contact(
        ContactUpdateInput.builder()
          .nickname(nickname)
          .profileImagePath(profileImagePath)
          .osType(osType)
          .pushToken(pushToken)
          .build())
        .build())
  }

  fun attachToken(
    phone: String,
    pushToken: String
  ): Observable<Response<AttachTokenToCatchUpContactMutation.Data>> {
    return from(
      AttachTokenToCatchUpContactMutation.builder()
        .phone(phone)
        .pushToken(pushToken)
        .osType(Define.PLATFORM_ANDROID)
        .build()
    )
  }

  fun createPromise(
    owner: String,
    name: String,
    dateTime: String,
    address: String,
    latitude: Double,
    longitude: Double
  ): Observable<Response<CreateCatchUpPromiseMutation.Data>> {
    return from(
      CreateCatchUpPromiseMutation.builder().data(
        CatchUpPromiseInput.builder()
          .owner(owner)
          .name(name)
          .dateTime(dateTime)
          .address(address)
          .latitude(latitude)
          .longitude(longitude)
          .build()
      ).build()
    )
  }

  fun updatePromise(
    id: String,
    owner: String,
    name: String,
    dateTime: String,
    address: String,
    latitude: Double,
    longitude: Double
  ): Observable<Response<UpdateCatchUpPromiseMutation.Data>> {
    return from(
      UpdateCatchUpPromiseMutation.builder().id(id).data(
        CatchUpPromiseInput.builder()
          .owner(owner)
          .name(name)
          .dateTime(dateTime)
          .address(address)
          .latitude(latitude)
          .longitude(longitude)
          .build()
      ).build()
    )
  }
}

