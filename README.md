# ActivityAnimation helper and ImageView transition With ViewPager

Just see WeChat IOS IOS IOS(not android) share moments,When u click friend's picture moments
or see gift below..
# FINAL EFFECT
![EFFECT](./show.gif)

#ActivityAnimation Usage
```java
  Pre activity onCreate()..
  ActivityTransitionEnterHelper.with(this).fromView(fromView).imageUrl(imgUrl).start(Test.class);
  Sub activity onCreate()..
  transitionExitHelper = ActivityTransitionExitHelper.with(getIntent()).toView(mImageView).background(mBackgroudnView).start(savedInstanceState);
                      @Override
                      public void onBackPressed() {
                          transitionExitHelper.runExitAnimation(new Runnable() {
                              @Override
                              public void run() {
                                  finish();
                              }
                          });
                      }
                      @Override
                      public void finish() {
                          super.finish();
                          // override transitions to skip the standard window animations
                          overridePendingTransition(0, 0);
                      }
```

#ImageView transition with ViewPager
 More see detail code..
 

#TODO
 add Loading State into viewPager

#THANKS
PhotoView from https://github.com/bm-x/PhotoView
Custom Activity Animations from https://www.youtube.com/watch?v=CPxkoe2MraA
