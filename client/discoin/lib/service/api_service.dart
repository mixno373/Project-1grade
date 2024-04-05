import 'package:dio/dio.dart';
import 'package:retrofit/http.dart';
import '../models/UserProfile.dart';

part 'api_service.g.dart';

@RestApi(baseUrl: 'https://diverstat.ru/discoin/')
abstract class ApiService {
  factory ApiService(Dio dio) = _ApiService;

  @GET('auth')
  Future<UserProfile> authorize();
}