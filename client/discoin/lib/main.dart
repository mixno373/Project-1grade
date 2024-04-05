import 'package:flutter/material.dart';

void main() {
  runApp(const MyApp());
}
class MyApp extends StatelessWidget {
  const MyApp({super.key});
  @override
  Widget build(BuildContext context) {
    return const MaterialApp(
      debugShowCheckedModeBanner: false,
      title: 'Discoin',
      home: LoginPage(),
    );
  }
}
class LoginPage extends StatefulWidget {
  const LoginPage({Key? key}) : super(key: key);
  @override
  State<LoginPage> createState() => _LoginPageState();
}
class _LoginPageState extends State<LoginPage> {
  final TextEditingController _loginController = TextEditingController();
  final TextEditingController _passwordController = TextEditingController();
  final _formKey = GlobalKey<FormState>();
  bool showPassword = false;
  bool isLoading = false;

  // Email Validation
  final loginPattern =
      r'^[a-zA-Z\-0-9]+$';
  bool validateLogin(String email) {
    final regExp = RegExp(loginPattern);
    return regExp.hasMatch(email);
  }

  void login() {
    if (_formKey.currentState!.validate()) {
      isLoading = true;
      setState(() {});
      Future.delayed(const Duration(seconds: 2), () {
        isLoading = false;
        setState(() {});
        Navigator.push(
          context,
          MaterialPageRoute(builder: (context) => const HomePage()),
        );
      });
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: Text('Discoin', style: TextStyle(color: Colors.white),), backgroundColor: Colors.cyan,),
      body: SingleChildScrollView(
        child: Container(
          padding: const EdgeInsets.symmetric(horizontal: 20, vertical: 40),
          child: Column(
              children: [
                const SizedBox(height: 80),
                const Align(
                  alignment: Alignment.center,
                  child: Text(
                    'Авторизация',
                    style: TextStyle(fontSize: 32, fontWeight: FontWeight.w200, fontFamily: 'Montserrat'),
                  ),
                ),

                Form(
                  key: _formKey,
                  child: Column(children: [
                    const SizedBox(height: 20),
                    TextFormField(
                      validator: (value) {
                        if (value!.isEmpty) {
                          return 'Пожалуйста, введите логин';
                        }
                        if (!validateLogin(_loginController.text)) {
                          return 'Пожалуйста, исправьте логин: a-z, A-Z, -, 0-9';
                        }
                        return null;
                      },
                      controller: _loginController,
                      style: const TextStyle(color: Colors.white),
                      decoration: InputDecoration(
                          filled: true,
                          hintText: 'Логин',
                          hintStyle: const TextStyle(color: Colors.white),
                          prefixIcon: const Icon(
                            Icons.person,
                            color: Colors.white,
                          ),
                          border: OutlineInputBorder(
                            borderRadius: BorderRadius.circular(36.0),
                            borderSide: BorderSide.none,
                          ),
                          fillColor: Colors.lightBlueAccent.shade200,
                          focusColor: Colors.white),
                    ),
                    const SizedBox(height: 25),
                    TextFormField(
                      validator: (value) {
                        if (value!.isEmpty) {
                          return 'Пожалуйста, введите пароль';
                        }
                        if (value.length < 6) {
                          return 'Пароль не может быть менее 6 символов';
                        }
                        if (value.length > 15) {
                          return 'Пароль не может быть более 15 символов';
                        }
                        return null;
                      },
                      controller: _passwordController,
                      obscureText: showPassword ? false : true,
                      style: const TextStyle(color: Colors.white),
                      decoration: InputDecoration(
                          filled: true,
                          hintText: 'Пароль',
                          hintStyle: const TextStyle(color: Colors.white),
                          prefixIcon: const Icon(
                            Icons.security,
                            color: Colors.white,
                          ),
                          suffixIcon: InkWell(
                              onTap: () {
                                setState(() {
                                  showPassword = !showPassword;
                                });
                              },
                              child: Icon(
                                  showPassword
                                      ? Icons.visibility_off
                                      : Icons.remove_red_eye,
                                  color: Colors.white)),
                          border: OutlineInputBorder(
                            borderRadius: BorderRadius.circular(36.0),
                            borderSide: BorderSide.none,
                          ),
                          fillColor: Colors.lightBlueAccent.shade200,
                          focusColor: Colors.white
                      ),
                    ),
                  ]),
                ),

                const SizedBox(height: 25),
                InkWell(
                  onTap: () {
                    login();
                  },
                  child: Container(
                    height: 50,
                    width: 120,
                    decoration: BoxDecoration(
                      color: Colors.lightBlueAccent.shade200,
                      borderRadius: BorderRadius.circular(36),
                    ),
                    child: Center(
                      child: isLoading
                          ? const Padding(
                        padding: EdgeInsets.all(8.0),
                        child: CircularProgressIndicator(
                          color: Colors.white,
                        ),
                      )
                          : const Text(
                        'Войти',
                        style: TextStyle(
                            color: Colors.white,
                            fontSize: 18,
                            fontWeight: FontWeight.w500),
                      ),
                    ),
                  ),
                ),
              ]),
        ),
      ),
    );
  }
}



class HomePage extends StatelessWidget {
  const HomePage({super.key});
  @override
  Widget build(BuildContext context) {
    return const Scaffold(
      body: Center(
        child: Text(
          'Ваш баланс: 373',
          style: TextStyle(fontSize: 22, fontWeight: FontWeight.w500, fontFamily: 'Montserrat'),
        ),
      ),
    );
  }
}