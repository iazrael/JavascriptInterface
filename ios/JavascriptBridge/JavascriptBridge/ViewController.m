//
//  ViewController.m
//  JavascriptBridge
//
//  Created by azrael on 13-7-21.
//  Copyright (c) 2013年 azrael. All rights reserved.
//

#import "ViewController.h"

@interface ViewController ()

@end

@implementation ViewController

- (void)webViewDidStartLoad:(UIWebView *)webView
{
    NSLog(@"start load");
}
- (void)webViewDidFinishLoad:(UIWebView *)webView
{
    NSLog(@"load end");
}

- (void)viewDidLoad
{
    [super viewDidLoad];
	// Do any additional setup after loading the view, typically from a nib.

    NSURL *url = [NSURL URLWithString:@"http://www.baidu.com"];
    NSURLRequest *req = [[NSURLRequest alloc] initWithURL: url];
    [mWebView loadRequest:req];
    
    mWebView.delegate = self;
}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}



@end
