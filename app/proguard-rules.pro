# ViewModels are created reflectively by the androidx ViewModelProvider,
# so their constructors must survive shrinking.
-keepclassmembers class * extends androidx.lifecycle.ViewModel {
    <init>(...);
}
